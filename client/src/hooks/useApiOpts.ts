"use client";

/**
 * API 엔드포인트 호출용 opts (AuthContext 기반).
 */
import { useCallback, useMemo } from "react";
import { useAuth } from "@/contexts/AuthContext";
import type { ApiEndpointsOptions } from "@/lib/api/endpoints";

export function useApiOpts(): ApiEndpointsOptions {
  const { accessToken, refreshToken } = useAuth();
  const onUnauthorized = useCallback(() => {
    if (typeof window !== "undefined") {
      window.location.href = "/login";
    }
  }, []);

  return useMemo(
    () => ({
      accessToken,
      onRefresh: refreshToken,
      onUnauthorized,
    }),
    [accessToken, refreshToken, onUnauthorized]
  );
}
