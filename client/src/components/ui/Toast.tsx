"use client";

import { createContext, useCallback, useContext, useState } from "react";

export type ToastVariant = "success" | "error" | "info" | "neutral";

export interface ToastItem {
  id: string;
  message: string;
  variant: ToastVariant;
}

interface ToastContextValue {
  toasts: ToastItem[];
  add: (message: string, variant?: ToastVariant) => void;
  remove: (id: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error("useToast must be used within ToastProvider");
  return ctx;
}

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const add = useCallback((message: string, variant: ToastVariant = "neutral") => {
    const id = `toast-${Date.now()}-${Math.random().toString(36).slice(2)}`;
    setToasts((prev) => [...prev, { id, message, variant }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4000);
  }, []);

  const remove = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ toasts, add, remove }}>
      {children}
      <ToastList />
    </ToastContext.Provider>
  );
}

const variantClasses: Record<ToastVariant, string> = {
  success: "border-green bg-green/15 text-green",
  error: "border-red bg-red/15 text-red",
  info: "border-cyan bg-cyan/15 text-cyan",
  neutral: "border border-borderSubtle bg-bg2 text-text-1",
};

function ToastList() {
  const ctx = useContext(ToastContext);
  if (!ctx) return null;
  const { toasts, remove } = ctx;

  if (toasts.length === 0) return null;

  return (
    <div
      className="fixed bottom-4 right-4 z-50 flex flex-col gap-2"
      role="region"
      aria-label="알림"
    >
      {toasts.map((t) => (
        <div
          key={t.id}
          role="alert"
          className={`flex items-center gap-2 rounded-token-md border px-4 py-3 text-sm shadow-lg ${variantClasses[t.variant]}`}
        >
          <span className="tabular-nums">{t.message}</span>
          <button
            type="button"
            onClick={() => remove(t.id)}
            className="ml-2 rounded p-0.5 opacity-70 hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-cyan focus:ring-offset-2 focus:ring-offset-bg2"
            aria-label="닫기"
          >
            <span aria-hidden>×</span>
          </button>
        </div>
      ))}
    </div>
  );
}
