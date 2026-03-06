/**
 * 백테스트 — 목록 + 실행 폼 구조만.
 * 실 API 호출 금지(mock/placeholder).
 */
export default function BacktestsPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">백테스트</h1>

      <section aria-label="실행 폼" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">실행 폼</h2>
        <div className="mt-3 text-sm text-text-3">
          markets / timeframes / period / initialCapital / fee (placeholder)
        </div>
      </section>

      <section aria-label="백테스트 목록" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">목록</h2>
        <div className="mt-3 overflow-x-auto">
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="border-b border-divider text-left text-text-3">
                <th className="py-2 pr-4">작업 ID</th>
                <th className="py-2 pr-4">상태</th>
                <th className="py-2 pr-4">생성 시각</th>
                <th className="py-2 pr-4">마켓/타임프레임</th>
              </tr>
            </thead>
            <tbody className="text-text-2">
              <tr className="border-b border-divider">
                <td colSpan={4} className="py-6 text-center text-text-3">
                  (목록 placeholder)
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
