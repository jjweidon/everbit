import Link from "next/link";

/**
 * 앱 푸터 — 전체 너비, 헤더와 독립.
 * docs/design/ui-ux-concept.md §4 레이아웃
 */
export function Footer() {
  return (
    <footer
      className="shrink-0 border-0 border-t border-borderSubtle bg-bg2 px-4 py-3"
      style={{ borderTopWidth: "0.5px" }}
      role="contentinfo"
    >
      <div className="mx-auto flex max-w-7xl items-center justify-between text-xs text-text-3">
        <span>Everbit v2</span>
        <nav className="flex items-center gap-4" aria-label="푸터 링크">
          <Link
            href="/settings/upbit-key"
            className="transition-colors hover:text-text-2"
          >
            설정
          </Link>
        </nav>
      </div>
    </footer>
  );
}
