"use client";

/**
 * API 데이터 + 에러 UX 훅.
 * 5xx는 toast, 403/429/418은 배너로 처리.
 * 네트워크 에러 연속 시 fetch 억제(무한 루프 방지), 토스트 중복 방지.
 */
import { useState, useCallback, useEffect, useRef } from "react";
import { useToast } from "@/components/ui/Toast";
import { ApiError } from "@/lib/api/errors";
import { ApiErrorBanner } from "@/components/errors/ApiErrorBanner";

export interface UseApiDataOptions<T> {
  fetch: () => Promise<T>;
  /** 초기 로딩 시 자동 fetch */
  enabled?: boolean;
  /** 404 시 에러 대신 이 값을 data로 사용(미구현 엔드포인트 대응) */
  fallbackOn404?: T;
}

export interface UseApiDataResult<T> {
  data: T | null;
  error: ApiError | null;
  loading: boolean;
  refetch: () => Promise<void>;
  /** 403/429/418 배너 렌더용 */
  ErrorBanner: () => React.ReactNode;
}

/** 네트워크 에러 시 토스트 중복 방지 */
let lastNetworkErrorAt = 0;
const NETWORK_ERROR_TOAST_COOLDOWN_MS = 5000;

/** 네트워크 에러 연속 발생 시 fetch 억제 (무한 루프 방지) */
let consecutiveNetworkErrors = 0;
const MAX_CONSECUTIVE_NETWORK_ERRORS = 6;

export function useApiData<T>({
  fetch,
  enabled = true,
  fallbackOn404,
}: UseApiDataOptions<T>): UseApiDataResult<T> {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(false);
  const toast = useToast();

  const fetchRef = useRef(fetch);
  const toastRef = useRef(toast);
  const fallbackRef = useRef(fallbackOn404);
  fetchRef.current = fetch;
  toastRef.current = toast;
  fallbackRef.current = fallbackOn404;

  const doFetch = useCallback(
    async (opts?: { force?: boolean }) => {
      const skipDueToThrottle =
        !opts?.force &&
        consecutiveNetworkErrors >= MAX_CONSECUTIVE_NETWORK_ERRORS;
      if (skipDueToThrottle) return;

      setLoading(true);
      setError(null);
      try {
        const result = await fetchRef.current();
        consecutiveNetworkErrors = 0;
        setData(result);
      } catch (e) {
        if (e instanceof ApiError) {
          const useFallback =
            e.status === 404 && fallbackRef.current !== undefined;
          if (useFallback) {
            consecutiveNetworkErrors = 0;
            setData(fallbackRef.current as T);
            setError(null);
          } else {
            consecutiveNetworkErrors = 0;
            setError(e);
            if (e.is5xx) {
              toastRef.current.add(
                e.body.message + " 잠시 후 다시 시도해 주세요.",
                "error"
              );
            }
          }
        } else {
          setError(null);
          consecutiveNetworkErrors += 1;
          const now = Date.now();
          if (now - lastNetworkErrorAt > NETWORK_ERROR_TOAST_COOLDOWN_MS) {
            lastNetworkErrorAt = now;
            toastRef.current.add("네트워크 오류가 발생했습니다.", "error");
          }
        }
      } finally {
        setLoading(false);
      }
    },
    []
  );

  useEffect(() => {
    if (!enabled) return;
    if (consecutiveNetworkErrors >= MAX_CONSECUTIVE_NETWORK_ERRORS) return;
    doFetch();
  }, [enabled, doFetch]);

  const refetch = useCallback(() => doFetch({ force: true }), [doFetch]);

  const ErrorBanner = useCallback(() => {
    if (!error || error.is5xx) return null;
    return (
      <ApiErrorBanner
        error={error}
        onDismiss={() => setError(null)}
      />
    );
  }, [error]);

  return {
    data,
    error,
    loading,
    refetch,
    ErrorBanner,
  };
}
