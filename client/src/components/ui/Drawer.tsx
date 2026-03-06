"use client";

import { useEffect, useRef } from "react";

export interface DrawerProps {
  open: boolean;
  onClose: () => void;
  /** 제목 (선택) */
  title?: string;
  children: React.ReactNode;
  /** 드로어 폭 (기본 400px) */
  width?: string | number;
}

export function Drawer({
  open,
  onClose,
  title,
  children,
  width = "400px",
}: DrawerProps) {
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

  const w = typeof width === "number" ? `${width}px` : width;

  return (
    <div
      className="fixed inset-0 z-50 flex justify-end"
      role="dialog"
      aria-modal="true"
      aria-labelledby={title ? "drawer-title" : undefined}
    >
      <div
        className="absolute inset-0 bg-bg0/60"
        aria-hidden
        onClick={onClose}
      />
      <div
        ref={ref}
        className="relative flex h-full flex-col border-l border-border bg-bg2 shadow-lg"
        style={{ width: w }}
        onClick={(e) => e.stopPropagation()}
      >
        {title && (
          <h2
            id="drawer-title"
            className="border-b border-divider px-4 py-3 text-sm font-medium text-text-1"
          >
            {title}
          </h2>
        )}
        <div className="flex-1 overflow-y-auto p-4">{children}</div>
      </div>
    </div>
  );
}
