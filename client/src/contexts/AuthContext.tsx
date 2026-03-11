"use client";

/**
 * 인증 컨텍스트 — Access Token 메모리 저장, refresh 플로우.
 * SoT: docs/ui/everbit_ui_impl_spec.md §3, ADR-0007
 *
 * 부트스트랩 우선순위:
 *  1) sessionStorage "everbit_at_bootstrap" — OAuth 콜백(/auth/complete)이 심어 둔 토큰
 *  2) POST /api/v2/auth/refresh — HttpOnly 쿠키로 서버에서 재발급 (페이지 새로고침 시)
 */
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  useMemo,
  type ReactNode,
} from "react";
import { getApiBase, API_BASE_PATH } from "@/lib/api/config";
import type { AuthRefreshResponse } from "@/types/api-contracts";

interface AuthContextValue {
  accessToken: string | null;
  setAccessToken: (token: string | null) => void;
  /** 401 시 1회 호출. 성공 시 새 토큰 반환, 실패 시 null */
  refreshToken: () => Promise<string | null>;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}

const BOOTSTRAP_KEY = "everbit_at_bootstrap";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(null);

  // 동시 refresh 요청을 하나의 Promise로 합쳐 JTI 재사용 탐지 방지
  const refreshInflight = useRef<Promise<string | null> | null>(null);
  // StrictMode dev 이중 effect 실행 방어 (ref는 remount에서도 유지됨)
  const bootstrapAttempted = useRef(false);

  const refreshToken = useCallback(async (): Promise<string | null> => {
    if (refreshInflight.current) return refreshInflight.current;

    const url = `${getApiBase()}${API_BASE_PATH}/auth/refresh`;
    refreshInflight.current = (async () => {
      try {
        const res = await fetch(url, {
          method: "POST",
          credentials: "include",
        });
        if (!res.ok) return null;
        const data = (await res.json()) as AuthRefreshResponse;
        const token = data.accessToken;
        if (token) {
          setAccessToken(token);
          return token;
        }
        return null;
      } catch {
        return null;
      } finally {
        refreshInflight.current = null;
      }
    })();

    return refreshInflight.current;
  }, []);

  // 마운트 시 토큰 부트스트랩:
  //  - OAuth 콜백 직후: sessionStorage에서 즉시 픽업 (네트워크 왕복 없음)
  //  - 일반 페이지 진입: HttpOnly 쿠키로 서버 refresh 시도
  useEffect(() => {
    if (bootstrapAttempted.current) return;
    bootstrapAttempted.current = true;

    const bootstrap = sessionStorage.getItem(BOOTSTRAP_KEY);
    if (bootstrap) {
      sessionStorage.removeItem(BOOTSTRAP_KEY);
      setAccessToken(bootstrap);
      return;
    }
    refreshToken();
  }, [refreshToken]);

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken,
      setAccessToken,
      refreshToken,
      isAuthenticated: !!accessToken,
    }),
    [accessToken, refreshToken]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
