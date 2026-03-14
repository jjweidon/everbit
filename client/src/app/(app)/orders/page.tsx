"use client";

/**
 * 주문 목록 — 필터 + 테이블, THROTTLED next_retry_at 노출
 * docs/ui/everbit_ui_impl_spec.md §5.6
 * API: GET /api/v2/orders, /dashboard/summary(risk 배너용)
 */
import { useState } from "react";
import Link from "next/link";
import { InfoTooltip } from "@/components/ui/InfoTooltip";
import { SeverityBanner } from "@/components/ui/SeverityBanner";
import { StatusChip, TagBadge, SideBadge, IntentTypeBadge, Drawer } from "@/components/ui";
import { TERM_TOOLTIPS } from "@/lib/term-tooltips";
import { useApiData } from "@/hooks/useApiData";
import { useApiOpts } from "@/hooks/useApiOpts";
import { getOrders, getDashboardSummary } from "@/lib/api/endpoints";
import type { OrderListItem, AttemptStatus } from "@/types/api-contracts";

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

function OrderDrawerContent({ order }: { order: OrderListItem }) {
  const a = order.latestAttempt;
  return (
    <div className="space-y-4 text-sm">
      <div>
        <p className="text-text-3">의도 ID</p>
        <p className="font-mono text-text-1">{order.intentPublicId}</p>
      </div>
      <div>
        <p className="text-text-3">시도 상태</p>
        <StatusChip
          tone={getAttemptStatusTone(a.status)}
          label={
            a.status === "THROTTLED" && a.nextRetryAt
              ? `429 THROTTLED ~${formatIso(a.nextRetryAt)}`
              : a.status
          }
        />
      </div>
      {a.errorCode && (
        <div>
          <p className="text-text-3">에러 코드</p>
          <p className="text-red">{a.errorCode}</p>
        </div>
      )}
      {order.latestAttempt.upbitUuid && (
        <Link
          href={`/orders/${order.latestAttempt.upbitUuid}`}
          className="inline-block text-cyan hover:underline"
        >
          상세 보기 →
        </Link>
      )}
    </div>
  );
}

export default function OrdersPage() {
  const [selected, setSelected] = useState<OrderListItem | null>(null);
  const opts = useApiOpts();
  const ordersRes = useApiData({
    fetch: () => getOrders({ limit: 50 }, opts),
    enabled: true,
    fallbackOn404: { items: [], nextCursor: null },
  });
  const dashboardRes = useApiData({
    fetch: () => getDashboardSummary(opts),
    enabled: true,
  });

  const orderList = ordersRes.data?.items ?? [];
  const risk = dashboardRes.data?.risk;
  const hasUnknown = (risk?.unknownAttempts24h ?? 0) > 0;
  const hasSuspended = (risk?.suspendedMarkets?.length ?? 0) > 0;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">주문</h1>

      {/* API 에러 배너 */}
      {ordersRes.ErrorBanner()}
      {dashboardRes.ErrorBanner()}

      {/* 위험 배너 (데이터 기반) */}
      <div className="space-y-2">
        {hasUnknown && risk && (
          <SeverityBanner
            type="UNKNOWN"
            detail={`24h 내 ${risk.unknownAttempts24h}건. reconcile로 확정.`}
          />
        )}
        {hasSuspended && risk && (
          <SeverityBanner
            type="SUSPENDED"
            detail={`마켓: ${risk.suspendedMarkets.join(", ")}`}
          />
        )}
      </div>

      <section aria-label="필터" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">필터</h2>
        <div className="mt-3 flex flex-wrap gap-2 text-sm text-text-3">
          마켓 / 의도 유형 / 시도 상태 / 기간 (mock에서는 비활성)
        </div>
      </section>

      <section aria-label="주문 목록" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">주문 목록</h2>
        {ordersRes.loading && orderList.length === 0 ? (
          <p className="mt-3 text-text-3">로딩 중…</p>
        ) : ordersRes.error && orderList.length === 0 ? (
          <p className="mt-3 text-text-3">데이터를 불러올 수 없습니다.</p>
        ) : (
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
                <th className="py-2 pr-4">next_retry_at</th>
                <th className="py-2 pr-4">Upbit 주문 ID</th>
                <th className="py-2 pr-4">사유 코드</th>
              </tr>
            </thead>
            <tbody className="text-text-2">
              {orderList.map((o) => (
                <tr
                  key={o.intentPublicId}
                  className="cursor-pointer border-b border-divider transition-colors hover:bg-bg2"
                  onClick={() => setSelected(o)}
                >
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
                  <td className="py-2 pr-4 tabular-nums">{o.latestAttempt.attemptNo}</td>
                  <td className="py-2 pr-4 tabular-nums text-text-3">
                    {o.latestAttempt.nextRetryAt
                      ? formatIso(o.latestAttempt.nextRetryAt)
                      : "—"}
                  </td>
                  <td className="py-2 pr-4 font-mono text-xs">
                    {o.latestAttempt.upbitUuid ? (
                      <Link
                        href={`/orders/${o.latestAttempt.upbitUuid}`}
                        className="text-cyan hover:underline"
                        onClick={(e) => e.stopPropagation()}
                      >
                        {o.latestAttempt.upbitUuid.slice(0, 8)}…
                      </Link>
                    ) : (
                      "—"
                    )}
                  </td>
                  <td className="py-2 pr-4 text-text-3">{o.reasonCode ?? "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        )}
      </section>

      <Drawer
        open={!!selected}
        onClose={() => setSelected(null)}
        title={selected ? `주문 ${selected.intentPublicId}` : ""}
        width={420}
      >
        {selected && <OrderDrawerContent order={selected} />}
      </Drawer>
    </div>
  );
}
