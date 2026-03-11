"use client";

/**
 * API 데이터 + 에러 UX 훅.
 * 5xx는 toast, 403/429/418은 배너로 처리.
 */
import { useState, useCallback, useEffect, useRef } from "react";
import { useToast } from "@/components/ui/Toast";
import { ApiError } from "@/lib/api/errors";
import { ApiErrorBanner } from "@/components/errors/ApiErrorBanner";

export interface UseApiDataOptions<T> {
  fetch: () => Promise<T>;
  /** 초기 로딩 시 자동 fetch */
  enabled?: boolean;
}

export interface UseApiDataResult<T> {
  data: T | null;
  error: ApiError | null;
  loading: boolean;
  refetch: () => Promise<void>;
  /** 403/429/418 배너 렌더용 */
  ErrorBanner: () => React.ReactNode;
}

export function useApiData<T>({
  fetch,
  enabled = true,
}: UseApiDataOptions<T>): UseApiDataResult<T> {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(false);
  const toast = useToast();

  const fetchRef = useRef(fetch);
  const toastRef = useRef(toast);
  fetchRef.current = fetch;
  toastRef.current = toast;

  const doFetch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await fetchRef.current();
      setData(result);
    } catch (e) {
      if (e instanceof ApiError) {
        setError(e);
        if (e.is5xx) {
          toastRef.current.add(
            e.body.message + " 잠시 후 다시 시도해 주세요.",
            "error"
          );
        }
      } else {
        toastRef.current.add("네트워크 오류가 발생했습니다.", "error");
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (enabled) doFetch();
  }, [enabled, doFetch]);

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
    refetch: doFetch,
    ErrorBanner,
  };
}
