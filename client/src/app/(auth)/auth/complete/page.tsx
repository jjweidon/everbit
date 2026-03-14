"use client";

/**
 * OAuth2 콜백 랜딩 페이지.
 * 백엔드가 로그인 성공 후 리다이렉트하는 URL:
 *   {frontendBaseUrl}/auth/complete#access_token=<JWT>
 *
 * 처리 흐름:
 *  1) URL 해시에서 access_token 추출
 *  2) sessionStorage("everbit_at_bootstrap")에 저장
 *  3) GET /api/v2/upbit/key/status 호출 후:
 *     - NOT_REGISTERED 또는 VERIFICATION_FAILED → /upbit-key
 *     - REGISTERED 또는 오류 시 → /dashboard
 *
 * 에러가 있는 경우 → /login?error=<message>
 */
import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense } from "react";
import { getApiBase, API_BASE_PATH } from "@/lib/api/config";

const BOOTSTRAP_KEY = "everbit_at_bootstrap";

const UPBIT_KEY_STATUS_PATH = "/upbit/key/status";

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

    if (!token) {
      router.replace("/dashboard");
      return;
    }

    sessionStorage.setItem(BOOTSTRAP_KEY, token);

    const decideRedirect = async () => {
      try {
        const res = await fetch(
          `${getApiBase()}${API_BASE_PATH}${UPBIT_KEY_STATUS_PATH}`,
          {
            method: "GET",
            headers: { Authorization: `Bearer ${token}` },
            credentials: "omit",
          }
        );
        if (res.ok) {
          const data = (await res.json()) as { status: string };
          if (
            data.status === "NOT_REGISTERED" ||
            data.status === "VERIFICATION_FAILED"
          ) {
            router.replace("/upbit-key");
            return;
          }
        }
      } catch {
        // 네트워크 오류 등 → 대시보드로 보냄 (대시보드에서 키 없으면 리다이렉트)
      }
      router.replace("/dashboard");
    };

    decideRedirect();
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
