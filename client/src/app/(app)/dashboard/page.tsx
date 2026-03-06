/**
 * 대시보드 — 실행·리스크, 자산·손익, 최근 주문, 마켓 상태 구조만.
 * 실 API 호출 금지(mock/placeholder).
 * 전문 용어는 InfoTooltip으로 도움말 제공.
 */
import { InfoTooltip } from "@/components/ui/InfoTooltip";
import { TERM_TOOLTIPS } from "@/lib/term-tooltips";

export default function DashboardPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">대시보드</h1>

      {/* 실행·리스크 */}
      <section aria-label="실행 및 리스크" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="inline-flex items-center text-sm font-medium text-text-2">
          실행·리스크
          <InfoTooltip content={TERM_TOOLTIPS.EXECUTION_RISK} ariaLabel="실행·리스크 설명" />
        </h2>
        <div className="mt-3 grid grid-cols-2 gap-4 sm:grid-cols-4">
          {[
            { label: "킬 스위치", tip: TERM_TOOLTIPS.KILL_SWITCH },
            { label: "전략", tip: "자동매매 전략(EXTREME_FLIP) 활성화 여부." },
            { label: "마지막 오류", tip: "가장 최근 발생한 오류 시각." },
            { label: "UNKNOWN 건수", tip: TERM_TOOLTIPS.UNKNOWN_COUNT },
          ].map(({ label, tip }) => (
            <div key={label} className="rounded border border-border bg-bg1 p-3">
              <p className="inline-flex items-center text-xs text-text-3">
                {label}
                <InfoTooltip content={tip} ariaLabel={`${label} 설명`} />
              </p>
              <p className="mt-1 text-text-1">—</p>
            </div>
          ))}
        </div>
      </section>

      {/* 자산·손익 */}
      <section aria-label="자산 및 손익" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="inline-flex items-center text-sm font-medium text-text-2">
          자산·손익
          <InfoTooltip content={TERM_TOOLTIPS.EQUITY_PNL} ariaLabel="자산·손익 설명" />
        </h2>
        <div className="mt-3 grid grid-cols-2 gap-4 sm:grid-cols-3">
          {[
            { label: "자산", tip: TERM_TOOLTIPS.EQUITY },
            { label: "실현손익", tip: TERM_TOOLTIPS.REALIZED },
            { label: "미실현손익", tip: TERM_TOOLTIPS.UNREALIZED },
          ].map(({ label, tip }) => (
            <div key={label} className="rounded border border-border bg-bg1 p-3">
              <p className="inline-flex items-center text-xs text-text-3">
                {label}
                <InfoTooltip content={tip} ariaLabel={`${label} 설명`} />
              </p>
              <p className="mt-1 tabular-nums text-text-1">—</p>
            </div>
          ))}
        </div>
      </section>

      {/* 최근 주문 (ACK 기준) */}
      <section aria-label="최근 주문" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">최근 주문 (접수 완료 기준)</h2>
        <div className="mt-3 overflow-x-auto">
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="border-b border-divider text-left text-text-3">
                <th className="py-2 pr-4">시각</th>
                <th className="py-2 pr-4">마켓</th>
                <th className="py-2 pr-4">매매</th>
                <th className="py-2 pr-4">의도 유형</th>
                <th className="py-2 pr-4">시도 상태</th>
                <th className="py-2 pr-4">Upbit 주문 ID</th>
              </tr>
            </thead>
            <tbody className="text-text-2">
              <tr className="border-b border-divider">
                <td colSpan={6} className="py-6 text-center text-text-3">
                  (목록 placeholder)
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      {/* 마켓 상태 테이블 */}
      <section aria-label="마켓 상태" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">마켓 상태</h2>
        <div className="mt-3 overflow-x-auto">
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="border-b border-divider text-left text-text-3">
                <th className="py-2 pr-4">마켓</th>
                <th className="py-2 pr-4">사용</th>
                <th className="py-2 pr-4">포지션 상태</th>
                <th className="py-2 pr-4">쿨다운 종료</th>
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
