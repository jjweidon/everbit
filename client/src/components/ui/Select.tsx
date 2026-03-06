"use client";

import { forwardRef } from "react";

export interface SelectOption {
  value: string;
  label: string;
}

export interface SelectProps
  extends Omit<
    React.SelectHTMLAttributes<HTMLSelectElement>,
    "className" | "children"
  > {
  options: SelectOption[];
  placeholder?: string;
  error?: boolean;
  className?: string;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  (
    {
      options,
      placeholder,
      error,
      className = "",
      ...rest
    },
    ref
  ) => (
    <select
      ref={ref}
      aria-invalid={error ?? undefined}
      className={`w-full rounded-token-md border bg-bg2 px-3 py-2 text-sm text-text-1 outline-none focus:ring-2 focus:ring-cyan focus:ring-offset-2 focus:ring-offset-bg1 disabled:cursor-not-allowed disabled:opacity-50 ${
        error ? "border-red" : "border-border"
      } ${className}`}
      {...rest}
    >
      {placeholder != null && (
        <option value="" disabled>
          {placeholder}
        </option>
      )}
      {options.map((opt) => (
        <option key={opt.value} value={opt.value}>
          {opt.label}
        </option>
      ))}
    </select>
  )
);
Select.displayName = "Select";
