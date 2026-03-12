"use client";

import { forwardRef } from "react";

export interface InputProps
  extends Omit<React.InputHTMLAttributes<HTMLInputElement>, "className"> {
  /** 에러 상태 시 시각적 표시 */
  error?: boolean;
  className?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ error, className = "", type = "text", ...rest }, ref) => (
    <input
      ref={ref}
      type={type}
      aria-invalid={error ?? undefined}
      className={`w-full rounded-token-md border bg-bg2 px-3 py-2 text-sm text-text-1 outline-none placeholder:text-text-3 focus:ring-2 focus:ring-cyan focus:ring-offset-2 focus:ring-offset-bg1 disabled:cursor-not-allowed disabled:opacity-50 ${
        error ? "border-red" : "border-borderSubtle"
      } ${className}`}
      {...rest}
    />
  )
);
Input.displayName = "Input";
