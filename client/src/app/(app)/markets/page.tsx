/**
 * 마켓 — 사용 여부/우선순위/SUSPENDED 관리 구조만.
 * 실 API 호출 금지(mock/placeholder).
 */
import { InfoTooltip } from "@/components/ui/InfoTooltip";
import { TERM_TOOLTIPS } from "@/lib/term-tooltips";

export default function MarketsPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">마켓</h1>

      <section aria-label="마켓 목록" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">마켓 목록</h2>
        <div className="mt-3 overflow-x-auto">
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="border-b border-divider text-left text-text-3">
                <th className="py-2 pr-4">마켓</th>
                <th className="py-2 pr-4">사용</th>
                <th className="py-2 pr-4">우선순위</th>
                <th className="py-2 pr-4">
                  <span className="inline-flex items-center">
                    포지션 상태
                    <InfoTooltip content={TERM_TOOLTIPS.POSITION_STATUS} ariaLabel="포지션 상태 설명" />
                  </span>
                </th>
                <th className="py-2 pr-4">마지막 신호 시각</th>
                <th className="py-2 pr-4">
                  <span className="inline-flex items-center">
                    쿨다운 종료
                    <InfoTooltip content={TERM_TOOLTIPS.COOLDOWN_UNTIL} ariaLabel="쿨다운 종료 설명" />
                  </span>
                </th>
                <th className="py-2 pr-4">동작</th>
              </tr>
            </thead>
            <tbody className="text-text-2">
              <tr className="border-b border-divider">
                <td colSpan={7} className="py-6 text-center text-text-3">
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
