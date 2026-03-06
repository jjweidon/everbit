"use client";

import {
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Send,
  Minus,
  type LucideIcon,
} from "lucide-react";
import { StatusBadge } from "./Badge";
import type { BadgeTone } from "./Badge";

/** StatusChip: 색 + 텍스트 동시 표시 (색만으로 의미 전달 금지). StatusBadge 기반. */
export type StatusChipTone = BadgeTone;

export interface StatusChipProps {
  tone: StatusChipTone;
  label: string;
  /** 선택: 아이콘. 미지정 시 톤별 기본 Lucide 아이콘 사용 */
  icon?: React.ReactNode;
  className?: string;
}

const defaultIcons: Record<StatusChipTone, LucideIcon> = {
  green: CheckCircle2,
  red: XCircle,
  yellow: AlertTriangle,
  cyan: Send,
  neutral: Minus,
};

export function StatusChip({
  tone,
  label,
  icon,
  className = "",
}: StatusChipProps) {
  const iconProp = icon ?? defaultIcons[tone];
  return (
    <StatusBadge
      tone={tone}
      label={label}
      icon={iconProp}
      className={className}
    />
  );
}
