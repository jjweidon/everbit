"use client";

import { useState, useRef, useCallback, useEffect } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  BarChart2,
  Crosshair,
  ListOrdered,
  History,
  Bell,
  KeyRound,
} from "lucide-react";

/** 사이드바 네비게이션 항목 (아이콘 + 라벨 + href) */
const NAV_ITEMS = [
  { href: "/dashboard", label: "대시보드", icon: LayoutDashboard },
  { href: "/markets", label: "마켓", icon: BarChart2 },
  { href: "/strategy", label: "전략", icon: Crosshair },
  { href: "/orders", label: "주문", icon: ListOrdered },
  { href: "/backtests", label: "백테스트", icon: History },
  { href: "/notifications", label: "알림", icon: Bell },
  { href: "/settings/upbit-key", label: "설정 (Upbit 키)", icon: KeyRound },
] as const;

/** 아이콘만 보이는 레일 너비 (Tailwind w-14 = 3.5rem = 56px) */
const SIDEBAR_RAIL_WIDTH = "w-14";
const SIDEBAR_RAIL_PX = 56;
/** 펼쳤을 때 패널 너비 (11rem = 176px, 여백 축소) */
const SIDEBAR_EXPANDED_PX = 176;
/** 호버 확장 트랜지션 시간 (docs/design/ui-ux-concept.md §7: 180~220ms) */
const TRANSITION_MS = 200;
/** 마우스 떠난 후 패널 유지 시간 (클릭 이동 가능) */
const COLLAPSE_DELAY_MS = 300;

/** 사이드바(데스크톱): 아이콘 레일 + 오버레이 라벨을 "아이템 단위로" 함께 렌더링 */
function SidebarNav({
  expanded,
  contentVisible,
}: {
  expanded: boolean;
  contentVisible: boolean;
}) {
  const pathname = usePathname();

  const overlayWidth = expanded ? SIDEBAR_EXPANDED_PX : 0;
  const itemWidth = SIDEBAR_RAIL_PX + overlayWidth;

  return (
    <aside
      className={`relative flex shrink-0 flex-col bg-bg2 ${SIDEBAR_RAIL_WIDTH} h-full min-h-0 ${
        expanded ? "border-0" : "border-0 border-r border-borderSubtle"
      }`}
      style={expanded ? undefined : { borderRightWidth: "0.5px" }}
      aria-label="사이드 내비게이션"
    >
      {/* 오버레이 배경 패널(리스트는 아이템 단위로 아래에서 렌더) */}
      <div
        className={`absolute left-14 top-0 bottom-0 z-10 overflow-hidden bg-bg2 shadow-lg ${
          expanded ? "border-0 border-r border-borderSubtle" : ""
        }`}
        style={{
          width: overlayWidth,
          transition: `width ${TRANSITION_MS}ms ease-out`,
          pointerEvents: "none",
          ...(expanded ? { borderRightWidth: "0.5px" } : {}),
        }}
        aria-hidden
      />

      {/* 접힌 상태: 배경은 아이콘(레일)만. 펼친 상태: 아이콘+라벨 전체. min-w-0으로 컨테이너 밖으로 안 넘어가게 */}
      <div
        className="relative z-20 flex min-w-0 flex-1 min-h-0 overflow-hidden"
        style={{
          width: itemWidth,
          transition: `width ${TRANSITION_MS}ms ease-out`,
        }}
      >
        <nav className="flex min-w-0 flex-1 flex-col gap-0.5 p-2" role="navigation">
          {NAV_ITEMS.map(({ href, label, icon: Icon }) => {
            const isActive =
              pathname === href || pathname.startsWith(href + "/");

            return (
              <Link
                key={href}
                href={href}
                className={`flex min-w-0 w-full items-center overflow-hidden whitespace-nowrap rounded-md text-text-2 transition-colors duration-150 [transition-property:color,background-color] ${
                  isActive
                    ? "bg-bg1 text-text-1"
                    : "hover:bg-bg1 hover:text-text-1"
                }`}
                title={label}
                aria-current={isActive ? "page" : undefined}
              >
                <span className="flex h-10 w-14 shrink-0 items-center justify-center">
                  <Icon
                    className="h-5 w-5 shrink-0"
                    strokeWidth={2}
                    aria-hidden
                  />
                </span>
                <span
                  className={`min-w-0 pr-2 text-sm font-medium ${
                    contentVisible ? "opacity-100" : "opacity-0"
                  } transition-opacity duration-75`}
                >
                  {label}
                </span>
              </Link>
            );
          })}
        </nav>
      </div>
    </aside>
  );
}

/**
 * 데스크톱용 사이드바: 아이콘 레일 + 호버 시 오버레이 확장.
 * 레일 위 또는 확장 패널 위에 마우스가 있으면 펼쳐진 상태 유지.
 */
export function Sidebar() {
  const [expanded, setExpanded] = useState(false);
  const [contentVisible, setContentVisible] = useState(false);
  const collapseTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const contentVisibleTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (expanded) {
      contentVisibleTimerRef.current = setTimeout(() => {
        setContentVisible(true);
        contentVisibleTimerRef.current = null;
      }, TRANSITION_MS);
    } else {
      if (contentVisibleTimerRef.current) {
        clearTimeout(contentVisibleTimerRef.current);
        contentVisibleTimerRef.current = null;
      }
      setContentVisible(false);
    }
    return () => {
      if (contentVisibleTimerRef.current) {
        clearTimeout(contentVisibleTimerRef.current);
      }
    };
  }, [expanded]);

  const clearCollapseTimer = useCallback(() => {
    if (collapseTimerRef.current) {
      clearTimeout(collapseTimerRef.current);
      collapseTimerRef.current = null;
    }
  }, []);

  const handleEnter = useCallback(() => {
    clearCollapseTimer();
    setExpanded(true);
  }, [clearCollapseTimer]);

  const handleLeave = useCallback(() => {
    clearCollapseTimer();
    collapseTimerRef.current = setTimeout(() => {
      setExpanded(false);
      collapseTimerRef.current = null;
    }, COLLAPSE_DELAY_MS);
  }, [clearCollapseTimer]);

  return (
    <div
      className="relative flex h-full min-h-0 shrink-0 flex-1"
      onMouseEnter={handleEnter}
      onMouseLeave={handleLeave}
    >
      <SidebarNav expanded={expanded} contentVisible={contentVisible} />
    </div>
  );
}

export {
  SIDEBAR_RAIL_WIDTH,
  TRANSITION_MS,
  COLLAPSE_DELAY_MS,
  NAV_ITEMS,
  SIDEBAR_EXPANDED_PX,
};

/** 모바일 드로어용 네비 링크 목록 (아이콘+라벨, 클릭 시 onNavigate 호출) */
export function MobileNavContent({
  onNavigate,
  currentPathname,
}: {
  onNavigate?: () => void;
  currentPathname: string;
}) {
  return (
    <nav className="flex flex-col gap-0.5 p-2" role="navigation">
      {NAV_ITEMS.map(({ href, label, icon: Icon }) => {
        const isActive =
          currentPathname === href || currentPathname.startsWith(href + "/");
        return (
          <Link
            key={href}
            href={href}
            onClick={onNavigate}
            className={`flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium transition-colors duration-150 ${
              isActive
                ? "bg-bg1 text-text-1"
                : "text-text-2 hover:bg-bg1 hover:text-text-1"
            }`}
            aria-current={isActive ? "page" : undefined}
          >
            <Icon className="h-5 w-5 shrink-0" strokeWidth={2} aria-hidden />
            <span>{label}</span>
          </Link>
        );
      })}
    </nav>
  );
}
