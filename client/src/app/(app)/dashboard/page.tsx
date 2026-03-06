"use client";

/**
 * 대시보드 — 실행·리스크, 자산·손익, 최근 주문, 마켓 상태
 * docs/ui/everbit_ui_impl_spec.md §5.3
 * API: GET /api/v2/dashboard/summary, /orders, /markets
 */
import Link from "next/link";
import { InfoTooltip } from "@/components/ui/InfoTooltip";
import { SeverityBanner } from "@/components/ui/SeverityBanner";
import { StatusChip, TagBadge, OnOffBadge, SideBadge, IntentTypeBadge } from "@/components/ui";
import { TERM_TOOLTIPS } from "@/lib/term-tooltips";
import { useApiData } from "@/hooks/useApiData";
import { useApiOpts } from "@/hooks/useApiOpts";
import { getDashboardSummary, getOrders, getMarkets } from "@/lib/api/endpoints";
import type { AttemptStatus } from "@/types/api-contracts";

function formatKrw(v: string) {
  return new Intl.NumberFormat("ko-KR").format(Number(v)) + " 원";
}

function formatIso(iso: string) {
  return new Date(iso).toLocaleString("ko-KR");
}

function getAttemptStatusTone(s: AttemptStatus): "green" | "red" | "yellow" | "cyan" | "neutral" {
  const map: Record<AttemptStatus, "green" | "red" | "yellow" | "cyan" | "neutral"> = {
    PREPARED: "neutral",
    SENT: "cyan",
    ACKED: "green",
    REJECTED: "red",
    THROTTLED: "yellow",
    UNKNOWN: "yellow",
    SUSPENDED: "yellow",
  };
  return map[s];
}

export default function DashboardPage() {
  const opts = useApiOpts();
  const dashboard = useApiData({
    fetch: () => getDashboardSummary(opts),
    enabled: true,
  });
  const orders = useApiData({
    fetch: () => getOrders({ limit: 20, onlyAcked: true }, opts),
    enabled: true,
  });
  const markets = useApiData({
    fetch: () => getMarkets(opts),
    enabled: true,
  });

  const summary = dashboard.data;
  const orderList = orders.data?.items ?? [];
  const marketList = markets.data ?? [];

  if (dashboard.loading && !summary) {
    return (
      <div className="flex items-center justify-center p-12 text-text-3">
        로딩 중…
      </div>
    );
  }

  if (!summary) {
    return (
      <div className="space-y-6">
        <h1 className="text-xl font-semibold text-text-1">대시보드</h1>
        {dashboard.ErrorBanner()}
        <p className="text-text-3">데이터를 불러올 수 없습니다.</p>
      </div>
    );
  }

  const risk = summary.risk;
  const equity = summary.equity;
  const strategyKey = summary.strategyKey;
  const strategyEnabled = summary.strategyEnabled ?? false;
  const accountEnabled = summary.accountEnabled ?? false;
  const lastErrorAt = summary.lastErrorAt;
  const lastReconcileAt = summary.lastReconcileAt;

  const hasUnknown = (risk.unknownAttempts24h ?? 0) > 0;
  const hasSuspended = (risk.suspendedMarkets?.length ?? 0) > 0;
  const has418 = !!risk.blocked418Until;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">대시보드</h1>

      {/* API 에러 배너 (403/429/418) */}
      {dashboard.ErrorBanner()}
      {orders.ErrorBanner()}
      {markets.ErrorBanner()}

      {/* 위험 배너 — UNKNOWN/SUSPENDED/418 (데이터 기반, 정책 위반 UX 아님) */}
      <div className="space-y-2">
        {hasUnknown && (
          <SeverityBanner
            type="UNKNOWN"
            detail={`24h 내 ${risk.unknownAttempts24h}건. reconcile로 확정 후 진행.`}
          />
        )}
        {hasSuspended && (
          <SeverityBanner
            type="SUSPENDED"
            detail={`마켓: ${risk.suspendedMarkets.join(", ")}. Markets 화면에서 수동 해제.`}
          />
        )}
        {has418 && (
          <SeverityBanner
            type="BLOCKED_418"
            detail={`차단 해제 예정: ${risk.blocked418Until ? formatIso(risk.blocked418Until) : "—"}`}
          />
        )}
      </div>

      {/* 실행·리스크 */}
      <section aria-label="실행 및 리스크" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="inline-flex items-center text-sm font-medium text-text-2">
          실행·리스크
          <InfoTooltip content={TERM_TOOLTIPS.EXECUTION_RISK} ariaLabel="실행·리스크 설명" />
        </h2>
        <div className="mt-3 grid grid-cols-2 gap-4 sm:grid-cols-4">
          <div className="rounded border border-border bg-bg1 p-3">
            <p className="inline-flex items-center text-xs text-text-3">
              킬 스위치
              <InfoTooltip content={TERM_TOOLTIPS.KILL_SWITCH} ariaLabel="킬 스위치 설명" />
            </p>
            <p className="mt-1">
              <OnOffBadge value={accountEnabled ?? false} />
            </p>
          </div>
          <div className="rounded border border-border bg-bg1 p-3">
            <p className="inline-flex items-center text-xs text-text-3">전략</p>
            <p className="mt-1 flex flex-wrap items-center gap-1.5 text-text-1">
              <TagBadge>{strategyKey}</TagBadge>
              <OnOffBadge value={strategyEnabled ?? false} />
            </p>
          </div>
          <div className="rounded border border-border bg-bg1 p-3">
            <p className="inline-flex items-center text-xs text-text-3">
              마지막 오류
              <InfoTooltip content={TERM_TOOLTIPS.LAST_ERROR} ariaLabel="마지막 오류 설명" />
            </p>
            <p className="mt-1 tabular-nums text-text-1">{lastErrorAt ? formatIso(lastErrorAt) : "—"}</p>
          </div>
          <div className="rounded border border-border bg-bg1 p-3">
            <p className="inline-flex items-center text-xs text-text-3">
              UNKNOWN 건수
              <InfoTooltip content={TERM_TOOLTIPS.UNKNOWN_COUNT} ariaLabel="UNKNOWN 건수 설명" />
            </p>
            <p className="mt-1 tabular-nums text-text-1">{risk.unknownAttempts24h}</p>
          </div>
          <div className="rounded border border-border bg-bg1 p-3 sm:col-span-2">
            <p className="inline-flex items-center text-xs text-text-3">마지막 reconcile</p>
            <p className="mt-1 tabular-nums text-text-1">
              {lastReconcileAt ? formatIso(lastReconcileAt) : "—"}
            </p>
          </div>
        </div>
      </section>

      {/* 자산·손익 */}
      <section aria-label="자산 및 손익" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="inline-flex items-center text-sm font-medium text-text-2">
          자산·손익
          <InfoTooltip content={TERM_TOOLTIPS.EQUITY_PNL} ariaLabel="자산·손익 설명" />
        </h2>
        <div className="mt-3 grid grid-cols-2 gap-4 sm:grid-cols-3">
          <div className="rounded border border-border bg-bg1 p-3">
            <p className="inline-flex items-center text-xs text-text-3">
              자산
              <InfoTooltip content={TERM_TOOLTIPS.EQUITY} ariaLabel="자산 설명" />
            </p>
            <p className="mt-1 tabular-nums text-text-1">{formatKrw(equity.equityKrw)}</p>
          </div>
          <div className="rounded border border-border bg-bg1 p-3">
            <p className="inline-flex items-center text-xs text-text-3">
              실현손익
              <InfoTooltip content={TERM_TOOLTIPS.REALIZED} ariaLabel="실현손익 설명" />
            </p>
            <p className="mt-1 tabular-nums text-text-1">{formatKrw(equity.realizedPnlKrw)}</p>
          </div>
          <div className="rounded border border-border bg-bg1 p-3">
            <p className="inline-flex items-center text-xs text-text-3">
              미실현손익
              <InfoTooltip content={TERM_TOOLTIPS.UNREALIZED} ariaLabel="미실현손익 설명" />
            </p>
            <p className="mt-1 tabular-nums text-text-1">{formatKrw(equity.unrealizedPnlKrw)}</p>
          </div>
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
              {orderList.slice(0, 5).map((o) => (
                <tr key={o.intentPublicId} className="border-b border-divider">
                  <td className="py-2 pr-4 tabular-nums">{formatIso(o.createdAt)}</td>
                  <td className="py-2 pr-4">
                    <TagBadge>{o.market}</TagBadge>
                  </td>
                  <td className="py-2 pr-4">
                    <SideBadge side={o.side} />
                  </td>
                  <td className="py-2 pr-4">
                    <IntentTypeBadge intentType={o.intentType} />
                  </td>
                  <td className="py-2 pr-4">
                    <StatusChip
                      tone={getAttemptStatusTone(o.latestAttempt.status)}
                      label={
                        o.latestAttempt.status === "THROTTLED" && o.latestAttempt.nextRetryAt
                          ? `429 ~${formatIso(o.latestAttempt.nextRetryAt)}`
                          : o.latestAttempt.status
                      }
                    />
                  </td>
                  <td className="py-2 pr-4 font-mono text-xs">
                    {o.latestAttempt.upbitUuid ? (
                      <Link
                        href={`/orders/${o.latestAttempt.upbitUuid}`}
                        className="text-cyan hover:underline"
                      >
                        {o.latestAttempt.upbitUuid.slice(0, 8)}…
                      </Link>
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
              {marketList.map((m) => (
                <tr key={m.market} className="border-b border-divider">
                  <td className="py-2 pr-4">
                    <TagBadge>{m.market}</TagBadge>
                  </td>
                  <td className="py-2 pr-4">
                    <OnOffBadge value={m.enabled} />
                  </td>
                  <td className="py-2 pr-4">
                    <StatusChip
                      tone={m.positionStatus === "SUSPENDED" ? "yellow" : "neutral"}
                      label={m.positionStatus}
                    />
                  </td>
                  <td className="py-2 pr-4 tabular-nums">
                    {m.cooldownUntil ? formatIso(m.cooldownUntil) : "—"}
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
