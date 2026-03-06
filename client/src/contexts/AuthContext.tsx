"use client";

/**
 * 인증 컨텍스트 — Access Token 메모리 저장, refresh 플로우.
 * SoT: docs/ui/everbit_ui_impl_spec.md §3, ADR-0007
 */
import {
  createContext,
  useCallback,
  useContext,
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

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(null);

  const refreshToken = useCallback(async (): Promise<string | null> => {
    const url = `${getApiBase()}${API_BASE_PATH}/auth/refresh`;
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
    }
  }, []);

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
