"use client";

/**
 * 루트 온보딩 페이지.
 * - 마운트 시 refresh 토큰 조용히 시도 → 세션 유효하면 /dashboard로 이동
 * - 세션 없으면 온보딩 UI + 카카오 로그인 버튼 표시
 */
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { getApiBase, API_BASE_PATH, getKakaoAuthUrl } from "@/lib/api/config";

export default function HomePage() {
  const router = useRouter();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    const checkSession = async () => {
      try {
        const res = await fetch(`${getApiBase()}${API_BASE_PATH}/auth/refresh`, {
          method: "POST",
          credentials: "include",
        });
        if (res.ok) {
          router.replace("/dashboard");
          return;
        }
      } catch {
        // 세션 없음 — 온보딩 표시
      }
      setIsChecking(false);
    };

    checkSession();
  }, [router]);

  if (isChecking) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-bg0">
        <div className="h-7 w-7 animate-spin rounded-full border-2 border-border border-t-text-3" />
      </div>
    );
  }

  return (
    <div className="relative flex min-h-screen flex-col items-center justify-center overflow-hidden bg-bg0 p-4">
      {/* 배경 그라데이션 후광 */}
      <div
        className="pointer-events-none absolute inset-0 flex items-center justify-center"
        aria-hidden
      >
        <div className="h-[480px] w-[480px] rounded-full bg-yellow/5 blur-[120px]" />
      </div>

      <div className="relative z-10 flex w-full max-w-sm flex-col items-center gap-8">
        {/* 로고 영역 */}
        <div className="flex flex-col items-center gap-4">
          <Image
            src="/images/everbit_coin_bounce_256_transparent.webp"
            alt="Everbit 코인"
            width={96}
            height={96}
            priority
            unoptimized
          />
          <div className="text-center">
            <h1 className="text-3xl font-bold tracking-tight text-text-1">
              Everbit
            </h1>
            <p className="mt-1.5 text-sm text-text-3">
              업비트 자동매매 플랫폼
            </p>
          </div>
        </div>

        {/* 기능 소개 */}
        <ul className="w-full space-y-2.5 rounded-lg border border-border bg-bg2 px-5 py-4">
          {FEATURES.map(({ icon, label }) => (
            <li key={label} className="flex items-center gap-3 text-sm text-text-2">
              <span className="text-base">{icon}</span>
              {label}
            </li>
          ))}
        </ul>

        {/* 카카오 로그인 버튼 */}
        <div className="flex w-full flex-col items-center gap-3">
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
          <p className="text-center text-xs text-text-3">
            최초 로그인 계정이 <span className="text-text-2">ADMIN</span>으로 고정됩니다.
          </p>
        </div>
      </div>
    </div>
  );
}

const FEATURES = [
  { icon: "⚡", label: "실시간 업비트 시세 및 호가 모니터링" },
  { icon: "🤖", label: "전략 기반 자동 주문 실행" },
  { icon: "📊", label: "백테스트 및 전략 성과 분석" },
  { icon: "🔔", label: "주문 체결 즉시 푸시 알림" },
] as const;
