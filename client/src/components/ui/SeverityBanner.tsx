"use client";

/**
 * SeverityBanner — UNKNOWN/SUSPENDED/418 발생 시 상단 배너
 * docs/ui/everbit_ui_impl_spec.md §4.2
 * UNKNOWN 재시도 버튼/자동 재주문 유도 UX 금지
 */
export type SeverityType = "UNKNOWN" | "SUSPENDED" | "BLOCKED_418";

export interface SeverityBannerProps {
  type: SeverityType;
  /** UNKNOWN: 건수, SUSPENDED: 마켓 목록, 418: 차단 만료 시각 */
  detail?: string;
  className?: string;
}

const config: Record<
  SeverityType,
  { label: string; ariaLabel: string; bgClass: string }
> = {
  UNKNOWN: {
    label: "UNKNOWN",
    ariaLabel: "주문 시도 결과 미확정 상태",
    bgClass: "border-yellow bg-yellow/15 text-yellow",
  },
  SUSPENDED: {
    label: "SUSPENDED",
    ariaLabel: "마켓 일시 중단 상태",
    bgClass: "border-yellow bg-yellow/15 text-yellow",
  },
  BLOCKED_418: {
    label: "418 차단",
    ariaLabel: "Upbit API 418 차단 상태",
    bgClass: "border-red bg-red/15 text-red",
  },
};

export function SeverityBanner({
  type,
  detail,
  className = "",
}: SeverityBannerProps) {
  const { label, ariaLabel, bgClass } = config[type];

  return (
    <div
      role="alert"
      aria-label={ariaLabel}
      className={`rounded-token-md border px-4 py-3 text-sm font-medium ${bgClass} ${className}`}
    >
      <span className="font-semibold">{label}</span>
      {detail && <span className="ml-2 text-text-2">{detail}</span>}
    </div>
  );
}
