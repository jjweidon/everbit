"use client";

export interface CardProps {
  children: React.ReactNode;
  /** 카드 제목 (선택) */
  title?: string;
  className?: string;
}

export function Card({ children, title, className = "" }: CardProps) {
  return (
    <section
      className={`rounded-token-lg border border-borderSubtle bg-bg2 ${className}`}
      aria-labelledby={title ? "card-title" : undefined}
    >
      {title && (
        <h2
          id="card-title"
          className="border-b border-borderSubtle px-4 py-3 text-sm font-medium text-text-heading"
        >
          {title}
        </h2>
      )}
      <div className="p-4">{children}</div>
    </section>
  );
}
