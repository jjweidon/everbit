"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const NAV_ITEMS = [
  { href: "/dashboard", label: "대시보드" },
  { href: "/markets", label: "마켓" },
  { href: "/strategy", label: "전략" },
  { href: "/orders", label: "주문" },
  { href: "/backtests", label: "백테스트" },
  { href: "/notifications", label: "알림" },
  { href: "/settings/upbit-key", label: "설정 (Upbit 키)" },
] as const;

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside
      className="flex w-56 flex-col border-r border-border bg-bg2"
      aria-label="사이드 내비게이션"
    >
      <div className="flex h-14 items-center border-b border-border px-4">
        <Link href="/dashboard" className="text-lg font-semibold text-text-1">
          Everbit v2
        </Link>
      </div>
      <nav className="flex flex-1 flex-col gap-0.5 p-2">
        {NAV_ITEMS.map(({ href, label }) => {
          const isActive = pathname === href || pathname.startsWith(href + "/");
          return (
            <Link
              key={href}
              href={href}
              className={`rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                isActive
                  ? "bg-bg1 text-text-1"
                  : "text-text-2 hover:bg-bg1 hover:text-text-1"
              }`}
            >
              {label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
