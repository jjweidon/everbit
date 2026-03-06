"use client";

import { useEffect, useRef } from "react";
import { Button } from "./Button";

export interface ConfirmModalProps {
  open: boolean;
  onClose: () => void;
  /** 제목 */
  title: string;
  /** 본문 */
  children: React.ReactNode;
  /** 확인 버튼 라벨 */
  confirmLabel: string;
  /** 취소 버튼 라벨 */
  cancelLabel?: string;
  /** 확인 시 실행 (닫기는 호출 측에서 onClose 호출) */
  onConfirm: () => void;
  /** 위험 액션(키 폐기 등) 시 빨간 버튼 */
  destructive?: boolean;
}

export function ConfirmModal({
  open,
  onClose,
  title,
  children,
  confirmLabel,
  cancelLabel = "취소",
  onConfirm,
  destructive = false,
}: ConfirmModalProps) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const handle = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", handle);
    return () => document.removeEventListener("keydown", handle);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-modal-title"
      aria-describedby="confirm-modal-desc"
    >
      <div
        className="absolute inset-0 bg-bg0/80"
        aria-hidden
        onClick={onClose}
      />
      <div
        ref={ref}
        className="relative w-full max-w-md rounded-token-lg border border-border bg-bg2 p-4 shadow-lg"
        onClick={(e) => e.stopPropagation()}
      >
        <h2
          id="confirm-modal-title"
          className="text-sm font-medium text-text-1"
        >
          {title}
        </h2>
        <div id="confirm-modal-desc" className="mt-2 text-sm text-text-2">
          {children}
        </div>
        <div className="mt-4 flex justify-end gap-2">
          <Button variant="secondary" onClick={onClose}>
            {cancelLabel}
          </Button>
          <Button
            variant={destructive ? "destructive" : "primary"}
            onClick={() => {
              onConfirm();
              onClose();
            }}
          >
            {confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  );
}
