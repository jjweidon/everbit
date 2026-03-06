/**
 * 주문 목록 — 필터 + 테이블 구조만.
 * 실 API 호출 금지(mock/placeholder).
 */
import { InfoTooltip } from "@/components/ui/InfoTooltip";
import { TERM_TOOLTIPS } from "@/lib/term-tooltips";

export default function OrdersPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">주문</h1>

      <section aria-label="필터" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">필터</h2>
        <div className="mt-3 flex flex-wrap gap-2 text-sm text-text-3">
          마켓 / 의도 유형 / 시도 상태 / 기간 (placeholder)
        </div>
      </section>

      <section aria-label="주문 목록" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">주문 목록</h2>
        <div className="mt-3 overflow-x-auto">
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="border-b border-divider text-left text-text-3">
                <th className="py-2 pr-4">생성 시각</th>
                <th className="py-2 pr-4">마켓</th>
                <th className="py-2 pr-4">매매</th>
                <th className="py-2 pr-4">
                  <span className="inline-flex items-center">
                    의도 유형
                    <InfoTooltip content={TERM_TOOLTIPS.INTENT_TYPE} ariaLabel="의도 유형 설명" />
                  </span>
                </th>
                <th className="py-2 pr-4">
                  <span className="inline-flex items-center">
                    시도 상태
                    <InfoTooltip content={TERM_TOOLTIPS.ATTEMPT_STATUS} ariaLabel="시도 상태 설명" />
                  </span>
                </th>
                <th className="py-2 pr-4">시도 번호</th>
                <th className="py-2 pr-4">Upbit 주문 ID</th>
                <th className="py-2 pr-4">사유 코드</th>
              </tr>
            </thead>
            <tbody className="text-text-2">
              <tr className="border-b border-divider">
                <td colSpan={8} className="py-6 text-center text-text-3">
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
