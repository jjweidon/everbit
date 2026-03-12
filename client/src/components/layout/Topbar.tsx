"use client";

import { useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { Bell, Menu } from "lucide-react";
import type { UpbitKeyStatus } from "@/types/api-contracts";
import { mockUpbitKeyStatus } from "@/lib/mocks/upbit-key";

/** Kill Switch placeholder — OFF 전환 시 confirm 필수(규칙). 실제 동작은 추후 연동. */
function KillSwitchPlaceholder() {
  const [on, setOn] = useState(true);
  const [pending, setPending] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const handleToggle = (next: boolean) => {
    if (next) {
      setOn(true);
      setShowConfirm(false);
      return;
    }
    setShowConfirm(true);
  };

  const handleConfirmOff = () => {
    setPending(true);
    setTimeout(() => {
      setOn(false);
      setPending(false);
      setShowConfirm(false);
    }, 300);
  };

  return (
    <div className="flex items-center gap-2">
      <span className="text-sm text-text-2">킬 스위치</span>
      <button
        type="button"
        role="switch"
        aria-checked={on}
        aria-label={on ? "자동매매 켜짐 (끄려면 클릭)" : "자동매매 꺼짐"}
        onClick={() => (on ? handleToggle(false) : handleToggle(true))}
        disabled={pending}
        className={`relative h-6 w-11 shrink-0 rounded-full border border-borderSubtle transition-colors ${
          on ? "bg-green/80" : "bg-bg2"
        } ${pending ? "opacity-60" : ""}`}
      >
        <span
          className={`absolute top-0.5 left-0.5 h-5 w-5 rounded-full bg-bg0 shadow transition-transform ${
            on ? "translate-x-5" : "translate-x-0"
          }`}
        />
      </button>
      {showConfirm && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-bg0/80"
          role="dialog"
          aria-modal="true"
          aria-labelledby="kill-switch-confirm-title"
        >
          <div className="w-full max-w-sm rounded-lg border border-borderSubtle bg-bg2 p-4 shadow-lg">
            <h2 id="kill-switch-confirm-title" className="font-medium text-text-heading">
              킬 스위치 끄기
            </h2>
            <p className="mt-2 text-sm text-text-2">
              자동매매가 중단됩니다. 주문 시도가 발생하지 않습니다.
            </p>
            <div className="mt-4 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setShowConfirm(false)}
                className="rounded-md border border-borderSubtle bg-bg1 px-3 py-1.5 text-sm text-text-1 hover:bg-bg2"
              >
                취소
              </button>
              <button
                type="button"
                onClick={handleConfirmOff}
                className="rounded-md bg-red px-3 py-1.5 text-sm font-medium text-bg0 hover:opacity-90"
              >
                끄기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/** Upbit 키 상태 — mock 데이터 기반 */
function UpbitKeyBadge() {
  const { status, lastVerifiedAt } = mockUpbitKeyStatus;
  const config: Record<UpbitKeyStatus, { label: string }> = {
    REGISTERED: { label: "키 등록됨" },
    NOT_REGISTERED: { label: "키 미등록" },
    VERIFICATION_FAILED: { label: "검증 실패" },
  };
  const label = config[status as UpbitKeyStatus].label;
  return (
    <Link
      href="/settings/upbit-key"
      className="flex items-center gap-2 rounded-md border border-borderSubtle bg-bg2 px-3 py-1.5 text-sm text-text-2 transition-colors hover:bg-bg1 hover:text-text-1"
      aria-label="Upbit 키 설정"
    >
      <span className="text-sm">{label}</span>
      {lastVerifiedAt && (
        <span className="text-xs text-text-3">
          검증 {new Date(lastVerifiedAt).toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" })}
        </span>
      )}
    </Link>
  );
}

/** 알림 슬롯 — Lucide Bell 아이콘 */
function NotificationsSlotPlaceholder() {
  return (
    <Link
      href="/notifications"
      className="flex items-center gap-2 rounded-md border border-borderSubtle bg-bg2 px-3 py-1.5 text-text-2 transition-colors hover:bg-bg1 hover:text-text-1"
      aria-label="알림 설정"
    >
      <Bell className="h-4 w-4 shrink-0" strokeWidth={2} aria-hidden />
      <span className="hidden text-sm sm:inline">알림</span>
    </Link>
  );
}

interface TopbarProps {
  /** 모바일에서 메뉴 버튼 클릭 시 호출 (미제공 시 메뉴 버튼 숨김) */
  onOpenMobileNav?: () => void;
}

export function Topbar({ onOpenMobileNav }: TopbarProps = {}) {
  return (
    <header
      className="flex h-14 shrink-0 items-center justify-between gap-2 border-0 border-b border-borderSubtle bg-bg2 px-3 sm:px-4"
      style={{ borderBottomWidth: "0.5px" }}
      role="banner"
    >
      <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2 sm:gap-4">
        <Link
          href="/dashboard"
          className="flex shrink-0 items-center transition-opacity hover:opacity-90"
          aria-label="Everbit 대시보드로 이동"
        >
          <Image
            src="/logos/everbit_logo_txt_white.png"
            alt="Everbit"
            width={120}
            height={28}
            className="h-7 w-auto"
            priority
          />
        </Link>
        {onOpenMobileNav && (
          <button
            type="button"
            onClick={onOpenMobileNav}
            className="flex h-9 w-9 shrink-0 items-center justify-center rounded-md border border-borderSubtle bg-bg2 text-text-2 transition-colors hover:bg-bg1 hover:text-text-1 md:hidden"
            aria-label="메뉴 열기"
          >
            <Menu className="h-5 w-5" strokeWidth={2} aria-hidden />
          </button>
        )}
        <KillSwitchPlaceholder />
        <UpbitKeyBadge />
      </div>
      <div className="flex shrink-0 items-center gap-2">
        <NotificationsSlotPlaceholder />
      </div>
    </header>
  );
}
