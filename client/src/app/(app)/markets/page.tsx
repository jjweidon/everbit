/**
 * 마켓 — 사용 여부/우선순위/SUSPENDED 관리
 * docs/ui/everbit_ui_impl_spec.md §5.4
 * SUSPENDED 해제는 Markets 화면에서만 가능(현재 disabled 표시)
 * Mock 데이터 사용, 실 API 호출 금지
 */
"use client";

import { InfoTooltip } from "@/components/ui/InfoTooltip";
import { SeverityBanner } from "@/components/ui/SeverityBanner";
import { StatusChip, TagBadge, OnOffBadge, Button } from "@/components/ui";
import { TERM_TOOLTIPS } from "@/lib/term-tooltips";
import { mockMarketStatusList } from "@/lib/mocks/markets";
import { mockDashboardSummary } from "@/lib/mocks/dashboard";

function formatIso(iso: string) {
  return new Date(iso).toLocaleString("ko-KR");
}

export default function MarketsPage() {
  const { risk } = mockDashboardSummary;
  const hasSuspended = risk.suspendedMarkets.length > 0;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">마켓</h1>

      {/* SUSPENDED 배너 */}
      {hasSuspended && (
        <SeverityBanner
          type="SUSPENDED"
          detail={`마켓: ${risk.suspendedMarkets.join(", ")}. 아래에서 수동 해제 가능.`}
        />
      )}

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
              {mockMarketStatusList.map((m) => (
                <tr key={m.market} className="border-b border-divider">
                  <td className="py-2 pr-4">
                    <TagBadge>{m.market}</TagBadge>
                  </td>
                  <td className="py-2 pr-4">
                    <OnOffBadge value={m.enabled} />
                  </td>
                  <td className="py-2 pr-4 tabular-nums">{m.priority}</td>
                  <td className="py-2 pr-4">
                    <StatusChip
                      tone={m.positionStatus === "SUSPENDED" ? "yellow" : "neutral"}
                      label={m.positionStatus}
                    />
                  </td>
                  <td className="py-2 pr-4 tabular-nums">
                    {m.lastSignalAt ? formatIso(m.lastSignalAt) : "—"}
                  </td>
                  <td className="py-2 pr-4 tabular-nums">
                    {m.cooldownUntil ? formatIso(m.cooldownUntil) : "—"}
                  </td>
                  <td className="py-2 pr-4">
                    {m.positionStatus === "SUSPENDED" ? (
                      <Button
                        variant="secondary"
                        disabled
                        aria-label="SUSPENDED 해제 (mock에서는 비활성)"
                      >
                        SUSPENDED 해제
                      </Button>
                    ) : (
                      "—"
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
