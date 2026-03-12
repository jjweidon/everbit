"use client";

/**
 * OAuth2 콜백 랜딩 페이지.
 * 백엔드가 로그인 성공 후 리다이렉트하는 URL:
 *   {frontendBaseUrl}/auth/complete#access_token=<JWT>
 *
 * 처리 흐름:
 *  1) URL 해시에서 access_token 추출
 *  2) sessionStorage("everbit_at_bootstrap")에 저장
 *     → (app) 레이아웃의 AuthProvider가 마운트 시 픽업
 *  3) /dashboard로 replace-redirect
 *
 * 에러가 있는 경우 → /login?error=<message>
 */
import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense } from "react";

const BOOTSTRAP_KEY = "everbit_at_bootstrap";

function AuthCompleteInner() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const errorParam = searchParams.get("error");
    if (errorParam) {
      router.replace(`/login?error=${encodeURIComponent(errorParam)}`);
      return;
    }

    const hash = window.location.hash.slice(1);
    const params = new URLSearchParams(hash);
    const token = params.get("access_token");

    if (token) {
      sessionStorage.setItem(BOOTSTRAP_KEY, token);
    }
    router.replace("/dashboard");
  }, [router, searchParams]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-bg0">
      <div className="h-7 w-7 animate-spin rounded-full border-2 border-borderSubtle border-t-text-3" />
    </div>
  );
}

export default function AuthCompletePage() {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-screen items-center justify-center bg-bg0">
          <div className="h-7 w-7 animate-spin rounded-full border-2 border-borderSubtle border-t-text-3" />
        </div>
      }
    >
      <AuthCompleteInner />
    </Suspense>
  );
}
