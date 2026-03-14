/**
 * 전략 설정 — 그룹/폼 영역
 * docs/ui/everbit_ui_impl_spec.md §5.5
 * Mock 데이터 사용, 실 API 호출 금지
 */
import { TagBadge } from "@/components/ui";
import { mockStrategyConfig } from "@/lib/mocks/strategy";

function formatIso(iso: string) {
  return new Date(iso).toLocaleString("ko-KR");
}

export default function StrategyPage() {
  const { strategyKey, configVersion, updatedAt, configJson } = mockStrategyConfig;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">전략</h1>

      <section aria-label="전략 메타" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">전략 정보</h2>
        <div className="mt-3 flex flex-wrap items-center gap-4 text-sm text-text-2">
          <span className="inline-flex items-center gap-1.5">
            strategyKey: <TagBadge>{strategyKey}</TagBadge>
          </span>
          <span>config_version: {configVersion}</span>
          <span>마지막 업데이트: {formatIso(updatedAt)}</span>
        </div>
      </section>

      <div className="grid gap-6 lg:grid-cols-[200px_1fr]">
        <section aria-label="파라미터 그룹" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
          <h2 className="text-sm font-medium text-text-2">그룹</h2>
          <ul className="mt-2 space-y-1 text-sm text-text-2">
            {["Timeframes", "Regime", "Entry", "Exit", "Risk", "Execution"].map((g) => (
              <li key={g}>{g}</li>
            ))}
          </ul>
        </section>
        <section aria-label="파라미터 폼" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
          <h2 className="text-sm font-medium text-text-2">파라미터</h2>
          <div className="mt-3 space-y-4">
            <pre className="overflow-x-auto rounded border border-thin border-borderSubtle bg-bg1 p-3 text-xs text-text-2">
              {JSON.stringify(configJson, null, 2)}
            </pre>
            <p className="text-sm text-text-3">(실제 편집 폼은 API 연동 후 구현)</p>
          </div>
        </section>
      </div>
    </div>
  );
}
