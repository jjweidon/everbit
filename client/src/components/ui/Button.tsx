"use client";

import { forwardRef } from "react";

type ButtonVariant = "primary" | "secondary" | "destructive" | "ghost";

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  /** 접근성: 버튼 목적 설명 (이미지만 있을 때 필수) */
  "aria-label"?: string;
}

const variantClasses: Record<ButtonVariant, string> = {
  primary:
    "bg-green text-bg0 border border-green hover:opacity-90 focus-visible:ring-2 focus-visible:ring-green focus-visible:ring-offset-2 focus-visible:ring-offset-bg1",
  secondary:
    "bg-bg2 text-text-1 border border-borderSubtle hover:bg-bg1 focus-visible:ring-2 focus-visible:ring-cyan focus-visible:ring-offset-2 focus-visible:ring-offset-bg1",
  destructive:
    "bg-red text-text-1 border border-red hover:opacity-90 focus-visible:ring-2 focus-visible:ring-red focus-visible:ring-offset-2 focus-visible:ring-offset-bg1",
  ghost:
    "bg-transparent text-text-1 hover:bg-bg2 border border-transparent focus-visible:ring-2 focus-visible:ring-cyan focus-visible:ring-offset-2 focus-visible:ring-offset-bg1",
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      variant = "secondary",
      type = "button",
      className = "",
      disabled,
      children,
      ...rest
    },
    ref
  ) => (
    <button
      ref={ref}
      type={type}
      disabled={disabled}
      className={`inline-flex items-center justify-center gap-2 rounded-token-md px-4 py-2 text-sm font-medium outline-none transition-opacity disabled:cursor-not-allowed disabled:opacity-50 ${variantClasses[variant]} ${className}`}
      {...rest}
    >
      {children}
    </button>
  )
);
Button.displayName = "Button";
