"use client";

/** StatusChip: 색 + 텍스트 동시 표시 (색만으로 의미 전달 금지) */
export type StatusChipTone =
  | "green"
  | "red"
  | "yellow"
  | "cyan"
  | "neutral";

export interface StatusChipProps {
  /** 톤(배경/테두리 색) */
  tone: StatusChipTone;
  /** 필수: 항상 텍스트로 상태를 표시 */
  label: string;
  /** 선택: 아이콘 */
  icon?: React.ReactNode;
  className?: string;
}

const toneClasses: Record<StatusChipTone, string> = {
  green: "border-green bg-green/15 text-green",
  red: "border-red bg-red/15 text-red",
  yellow: "border-yellow bg-yellow/15 text-yellow",
  cyan: "border-cyan bg-cyan/15 text-cyan",
  neutral: "border-border bg-bg2 text-text-2",
};

export function StatusChip({
  tone,
  label,
  icon,
  className = "",
}: StatusChipProps) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-token-md border px-2 py-0.5 text-xs font-medium tabular-nums ${toneClasses[tone]} ${className}`}
      role="status"
      aria-label={label}
    >
      {icon}
      <span>{label}</span>
    </span>
  );
}
