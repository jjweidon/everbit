"use client";

import { useState } from "react";
import Link from "next/link";

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
        className={`relative h-6 w-11 shrink-0 rounded-full border border-border transition-colors ${
          on ? "bg-green/80" : "bg-bg2"
        } ${pending ? "opacity-60" : ""}`}
      >
        <span
          className={`absolute top-0.5 left-0.5 h-5 w-5 rounded-full bg-bg0 shadow transition-transform ${
            on ? "translate-x-5" : "translate-x-0"
          }`}
        />
      </button>
      <span className="text-xs text-text-3">{on ? "ON" : "OFF"}</span>
      {showConfirm && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-bg0/80"
          role="dialog"
          aria-modal="true"
          aria-labelledby="kill-switch-confirm-title"
        >
          <div className="w-full max-w-sm rounded-lg border border-border bg-bg2 p-4 shadow-lg">
            <h2 id="kill-switch-confirm-title" className="font-medium text-text-1">
              킬 스위치 끄기
            </h2>
            <p className="mt-2 text-sm text-text-2">
              자동매매가 중단됩니다. 주문 시도가 발생하지 않습니다.
            </p>
            <div className="mt-4 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setShowConfirm(false)}
                className="rounded-md border border-border bg-bg1 px-3 py-1.5 text-sm text-text-1 hover:bg-border"
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

/** 연결 상태 placeholder — WS/Upbit 키 상태 등. */
function ConnectionStatusPlaceholder() {
  return (
    <div className="flex items-center gap-2 rounded-md border border-border bg-bg2 px-3 py-1.5">
      <span
        className="h-2 w-2 rounded-full bg-green"
        aria-hidden
        title="연결됨"
      />
      <span className="text-sm text-text-2">연결됨 (placeholder)</span>
    </div>
  );
}

/** 알림 슬롯 — 아이콘 + 배지 등. */
function NotificationsSlotPlaceholder() {
  return (
    <Link
      href="/notifications"
      className="flex items-center gap-1.5 rounded-md border border-border bg-bg2 px-3 py-1.5 text-text-2 transition-colors hover:bg-border hover:text-text-1"
      aria-label="알림 설정"
    >
      <span className="text-sm">🔔</span>
      <span className="text-sm">알림</span>
    </Link>
  );
}

export function Topbar() {
  return (
    <header
      className="flex h-14 shrink-0 items-center justify-between border-b border-border bg-bg2 px-4"
      role="banner"
    >
      <div className="flex items-center gap-6">
        <KillSwitchPlaceholder />
        <ConnectionStatusPlaceholder />
      </div>
      <div className="flex items-center gap-2">
        <NotificationsSlotPlaceholder />
      </div>
    </header>
  );
}
