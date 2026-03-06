"use client";

import { useId, useState } from "react";

interface InfoTooltipProps {
  /** 마우스 오버 시 표시할 도움말 내용 */
  content: string;
  /** 스크린 리더용 레이블 (기본: "도움말") */
  ariaLabel?: string;
  /** 추가 클래스 (컨테이너 span) */
  className?: string;
}

/**
 * 전문 용어 옆에 표시하는 정보 아이콘. 호버 시 도움말 툴팁 표시.
 * 퀀트/트레이딩 용어를 처음 보는 사용자용 설명 제공.
 */
export function InfoTooltip({ content, ariaLabel = "도움말", className = "" }: InfoTooltipProps) {
  const id = useId();
  const [visible, setVisible] = useState(false);

  return (
    <span
      className={`relative inline-flex align-middle ${className}`}
      onMouseEnter={() => setVisible(true)}
      onMouseLeave={() => setVisible(false)}
    >
      <span
        role="img"
        aria-label={ariaLabel}
        aria-describedby={visible ? id : undefined}
        className="ml-1 inline-flex h-4 w-4 cursor-help items-center justify-center rounded-full border-thin border-border bg-bg2 text-text-3 transition-colors hover:border-cyan hover:text-cyan"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 16 16"
          fill="currentColor"
          className="h-3 w-3"
          aria-hidden
        >
          <path d="M8 1a7 7 0 1 0 0 14A7 7 0 0 0 8 1Zm0 1.5a5.5 5.5 0 1 1 0 11 5.5 5.5 0 0 1 0-11ZM8 6a.75.75 0 0 1 .75.75v3.5a.75.75 0 0 1-1.5 0v-3.5A.75.75 0 0 1 8 6Zm0-2a.75.75 0 1 0 0 1.5.75.75 0 0 0 0-1.5Z" />
        </svg>
      </span>
      {visible && (
        <span
          id={id}
          role="tooltip"
          className="absolute left-full top-1/2 z-50 ml-1.5 w-56 -translate-y-1/2 rounded bg-bg2 px-2.5 py-2 text-xs leading-relaxed text-text-1 shadow-lg"
        >
          {content}
        </span>
      )}
    </span>
  );
}
