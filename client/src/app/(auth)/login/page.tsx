"use client";

/**
 * 로그인 페이지 — 401 리다이렉트 대상.
 * docs/ui/everbit_ui_impl_spec.md §5.1
 */
import Image from "next/image";
import { getKakaoAuthUrl } from "@/lib/api/config";

export default function LoginPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-bg0 p-4">
      <div className="flex w-full max-w-sm flex-col items-center gap-6">
        {/* 로고 */}
        <div className="flex flex-col items-center gap-3">
          <Image
            src="/images/everbit_coin_flip_512_transparent.webp"
            alt="Everbit 코인"
            width={72}
            height={72}
            priority
            unoptimized
          />
          <h1 className="text-2xl font-bold tracking-tight text-text-1">Everbit</h1>
        </div>

        {/* 세션 만료 안내 */}
        <div className="w-full rounded-lg border border-thin border-borderSubtle bg-bg2 px-5 py-4 text-center">
          <p className="text-sm font-medium text-text-1">세션이 만료되었습니다</p>
          <p className="mt-1 text-xs text-text-3">다시 로그인해주세요.</p>
        </div>

        {/* 카카오 로그인 버튼 */}
        <div className="flex w-full flex-col items-center gap-2">
          <a
            href={getKakaoAuthUrl()}
            className="block w-full transition-opacity hover:opacity-90 active:opacity-75"
          >
            <Image
              src="/images/kakao_login_button.png"
              alt="카카오로 로그인"
              width={400}
              height={60}
              className="h-auto w-full rounded-xl"
              priority
            />
          </a>
          <p className="text-xs text-text-3">
            최초 로그인 계정이 <span className="text-text-2">ADMIN</span>으로 고정됩니다.
          </p>
        </div>
      </div>
    </div>
  );
}
