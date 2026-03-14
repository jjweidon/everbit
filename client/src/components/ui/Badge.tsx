"use client";

import React from "react";
import { ShoppingBag, Coins } from "lucide-react";
import type { LucideIcon } from "lucide-react";

/** 상태 배지 톤 (색만으로 의미 전달 금지, 텍스트 필수) */
export type BadgeTone = "green" | "red" | "yellow" | "cyan" | "neutral";

const statusToneClasses: Record<BadgeTone, string> = {
  green: "border-green/50 bg-green/10 text-green dark:bg-green/15",
  red: "border-red/50 bg-red/10 text-red dark:bg-red/15",
  yellow: "border-yellow/50 bg-yellow/10 text-yellow dark:bg-yellow/15",
  cyan: "border-cyan/50 bg-cyan/10 text-cyan dark:bg-cyan/15",
  neutral: "border-divider bg-bg2 text-text-2",
};

const iconProps = {
  className: "h-3.5 w-3.5 shrink-0",
  strokeWidth: 2.25,
  "aria-hidden": true as const,
};

function isReactComponent(
  v: unknown
): v is React.ComponentType<Record<string, unknown>> {
  return (
    typeof v === "function" ||
    (typeof v === "object" && v !== null && "$$typeof" in v)
  );
}

export interface StatusBadgeProps {
  tone: BadgeTone;
  label: string;
  /** Lucide 아이콘 컴포넌트 또는 커스텀 ReactNode */
  icon?: LucideIcon | React.ReactNode;
  className?: string;
}

/** 상태 배지 — ACKED, SUSPENDED, THROTTLED 등 (아이콘+pill) */
export function StatusBadge({
  tone,
  label,
  icon: IconOrNode,
  className = "",
}: StatusBadgeProps) {
  const iconContent =
    IconOrNode == null
      ? null
      : React.isValidElement(IconOrNode)
        ? IconOrNode
        : isReactComponent(IconOrNode)
          ? React.createElement(IconOrNode, iconProps)
          : IconOrNode;
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-medium tabular-nums ${statusToneClasses[tone]} ${className}`}
      role="status"
      aria-label={label}
    >
      {(iconContent as React.ReactNode)}
      <span>{label}</span>
    </span>
  );
}

export interface TagBadgeProps {
  children: React.ReactNode;
  className?: string;
}

/** 태그 배지 — 마켓(KRW-BTC), 전략(EXTREME_FLIP) 등 (모노스페이스, 둥근 사각) */
export function TagBadge({ children, className = "" }: TagBadgeProps) {
  return (
    <span
      className={`inline-flex items-center rounded-md border border-borderSubtle bg-bg2 px-2 py-0.5 font-mono text-[11px] font-medium tracking-wide text-text-2 ${className}`}
    >
      {children}
    </span>
  );
}

export interface OnOffBadgeProps {
  value: boolean;
  className?: string;
}

/** ON/OFF 배지 — 킬 스위치, 전략, 마켓 사용 여부 (토글 칩: ON=채움, OFF=테두리만) */
export function OnOffBadge({ value, className = "" }: OnOffBadgeProps) {
  return (
    <span
      className={`inline-flex items-center rounded-md px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wider ${
        value
          ? "bg-emerald-500/90 text-white"
          : "border border-divider bg-transparent text-text-3"
      } ${className}`}
      role="status"
      aria-label={value ? "ON" : "OFF"}
    >
      {value ? "ON" : "OFF"}
    </span>
  );
}

export interface SideBadgeProps {
  side: "BUY" | "SELL";
  className?: string;
}

/** BUY/SELL 배지 — 매매 방향 (매수=쇼핑백, 매도=코인) */
export function SideBadge({ side, className = "" }: SideBadgeProps) {
  const isBuy = side === "BUY";
  return (
    <span
      className={`inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-xs font-semibold ${
        isBuy
          ? "bg-emerald-500/20 text-emerald-400"
          : "bg-rose-500/20 text-rose-400"
      } ${className}`}
      role="status"
      aria-label={side}
    >
      {isBuy ? (
        <ShoppingBag className="h-3.5 w-3.5" strokeWidth={2.5} aria-hidden />
      ) : (
        <Coins className="h-3.5 w-3.5" strokeWidth={2.5} aria-hidden />
      )}
      <span>{side}</span>
    </span>
  );
}

export type IntentType =
  | "ENTRY"
  | "EXIT_STOPLOSS"
  | "EXIT_TP"
  | "EXIT_TRAIL"
  | "EXIT_TIME";

export interface IntentTypeBadgeProps {
  intentType: IntentType;
  className?: string;
}

/** 의도 유형 배지 — ENTRY, EXIT_TP 등 (좌측 강조선, 모노스페이스, 라벨 스타일) */
const intentTypeConfig: Record<
  IntentType,
  { border: string; text: string }
> = {
  ENTRY: { border: "border-l-2 border-l-emerald-500 bg-emerald-500/5", text: "text-emerald-400" },
  EXIT_STOPLOSS: { border: "border-l-2 border-l-rose-500 bg-rose-500/5", text: "text-rose-400" },
  EXIT_TP: { border: "border-l-2 border-l-sky-500 bg-sky-500/5", text: "text-sky-400" },
  EXIT_TRAIL: { border: "border-l-2 border-l-violet-500 bg-violet-500/5", text: "text-violet-400" },
  EXIT_TIME: { border: "border-l-2 border-l-amber-500 bg-amber-500/5", text: "text-amber-400" },
};

export function IntentTypeBadge({ intentType, className = "" }: IntentTypeBadgeProps) {
  const { border, text } = intentTypeConfig[intentType];
  return (
    <span
      className={`inline-flex items-center pl-2 pr-2 py-0.5 font-mono text-[10px] font-medium ${border} ${text} ${className}`}
      role="status"
      aria-label={intentType}
    >
      {intentType}
    </span>
  );
}
