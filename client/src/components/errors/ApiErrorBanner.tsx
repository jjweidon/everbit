"use client";

/**
 * API 에러 배너 — 403/429/418 (docs/api/contracts.md §11.2)
 * UNKNOWN/SUSPENDED 정책 위반 UX는 추가하지 않음.
 */
import type { ApiError } from "@/lib/api/errors";

export type ApiErrorBannerType = "403" | "429" | "418";

export interface ApiErrorBannerProps {
  error: ApiError;
  onDismiss?: () => void;
  className?: string;
}

function formatRetryAt(seconds: number): string {
  const d = new Date(Date.now() + seconds * 1000);
  return d.toLocaleString("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

function formatIso(iso: string): string {
  return new Date(iso).toLocaleString("ko-KR");
}

export function ApiErrorBanner({ error, onDismiss, className = "" }: ApiErrorBannerProps) {
  if (error.is403) {
    return (
      <div
        role="alert"
        aria-label="권한 오류"
        className={`rounded-token-md border border-red bg-red/15 px-4 py-3 text-sm text-red ${className}`}
      >
        <span className="font-semibold">권한 오류</span>
        <span className="ml-2 text-text-2">{error.body.message}</span>
        {onDismiss && (
          <button
            type="button"
            onClick={onDismiss}
            className="ml-2 text-red/80 hover:text-red"
            aria-label="닫기"
          >
            ×
          </button>
        )}
      </div>
    );
  }

  if (error.is429) {
    const retryAt = error.retryAfter
      ? formatRetryAt(error.retryAfter)
      : error.body.message;
    return (
      <div
        role="alert"
        aria-label="요청 제한"
        className={`rounded-token-md border border-yellow bg-yellow/15 px-4 py-3 text-sm text-yellow ${className}`}
      >
        <span className="font-semibold">429 THROTTLED</span>
        <span className="ml-2 text-text-2">
          요청이 많습니다. {retryAt} 이후 재시도해 주세요.
        </span>
        {onDismiss && (
          <button
            type="button"
            onClick={onDismiss}
            className="ml-2 text-yellow/80 hover:text-yellow"
            aria-label="닫기"
          >
            ×
          </button>
        )}
      </div>
    );
  }

  if (error.is418Or503) {
    const until = error.blockedUntil ? formatIso(error.blockedUntil) : null;
    return (
      <div
        role="alert"
        aria-label="거래소 연동 중단"
        className={`rounded-token-md border border-red bg-red/15 px-4 py-3 text-sm text-red ${className}`}
      >
        <span className="font-semibold">418 안전 모드</span>
        <span className="ml-2 text-text-2">
          거래소 연동이 일시 중단되었습니다.
          {until && ` 차단 해제 예정: ${until}`}
        </span>
        {onDismiss && (
          <button
            type="button"
            onClick={onDismiss}
            className="ml-2 text-red/80 hover:text-red"
            aria-label="닫기"
          >
            ×
          </button>
        )}
      </div>
    );
  }

  return null;
}
