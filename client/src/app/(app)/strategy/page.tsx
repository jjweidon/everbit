/**
 * 전략 설정 — 그룹/폼 영역 구조만.
 * 실 API 호출 금지(mock/placeholder).
 */
export default function StrategyPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">전략</h1>

      <section aria-label="전략 메타" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">전략 정보</h2>
        <div className="mt-3 flex flex-wrap gap-4 text-sm text-text-2">
          <span>strategyKey: EXTREME_FLIP</span>
          <span>config_version: —</span>
          <span>마지막 업데이트: —</span>
        </div>
      </section>

      <div className="grid gap-6 lg:grid-cols-[200px_1fr]">
        <section aria-label="파라미터 그룹" className="rounded-lg border border-border bg-bg2 p-4">
          <h2 className="text-sm font-medium text-text-2">그룹</h2>
          <ul className="mt-2 space-y-1 text-sm text-text-2">
            {["Timeframes", "Regime", "Entry", "Exit", "Risk", "Execution"].map((g) => (
              <li key={g}>{g}</li>
            ))}
          </ul>
        </section>
        <section aria-label="파라미터 폼" className="rounded-lg border border-border bg-bg2 p-4">
          <h2 className="text-sm font-medium text-text-2">파라미터</h2>
          <div className="mt-3 space-y-4">
            <p className="text-text-3">(폼 placeholder)</p>
          </div>
        </section>
      </div>
    </div>
  );
}
