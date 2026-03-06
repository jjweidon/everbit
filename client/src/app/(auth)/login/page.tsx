/**
 * 로그인 — 401 리다이렉트 대상.
 * docs/ui/everbit_ui_impl_spec.md §5.1
 * 카카오 OAuth 연동은 추후 구현.
 */
import Link from "next/link";

export default function LoginPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-bg1 p-4">
      <div className="w-full max-w-sm rounded-lg border border-border bg-bg2 p-6 text-center">
        <h1 className="text-xl font-semibold text-text-1">로그인이 필요합니다</h1>
        <p className="mt-2 text-sm text-text-3">
          최초 로그인 계정이 OWNER로 고정됩니다. 다른 계정은 차단됩니다.
        </p>
        <div className="mt-6">
          <Link
            href="/dashboard"
            className="inline-block rounded-md border border-border bg-bg1 px-4 py-2 text-sm text-text-1 hover:bg-border"
          >
            대시보드로 돌아가기
          </Link>
        </div>
        <p className="mt-4 text-xs text-text-3">
          카카오 로그인 연동 예정
        </p>
      </div>
    </div>
  );
}
