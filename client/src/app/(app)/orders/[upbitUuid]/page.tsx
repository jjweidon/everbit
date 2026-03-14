/**
 * 주문 상세 — 딥링크 /orders/[upbitUuid]
 * docs/ui/everbit_ui_impl_spec.md §5.7
 * Mock 데이터 사용, 실 API 호출 금지
 */
import Link from "next/link";
import { StatusChip, SideBadge, IntentTypeBadge } from "@/components/ui";
import { mockOrderDetail, mockOrderList } from "@/lib/mocks/orders";
import type { AttemptStatus } from "@/types/api-contracts";

function formatIso(iso: string) {
  return new Date(iso).toLocaleString("ko-KR");
}

function formatKrw(v: string) {
  return new Intl.NumberFormat("ko-KR").format(Number(v)) + " 원";
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

interface OrderDetailPageProps {
  params: { upbitUuid: string };
}

export default async function OrderDetailPage({ params }: OrderDetailPageProps) {
  const { upbitUuid } = params;

  // Mock: uuid 일치 시 상세 반환
  const matched = mockOrderList.find((o) => o.latestAttempt.upbitUuid === upbitUuid);
  const detail =
    upbitUuid === mockOrderDetail.upbitUuid
      ? mockOrderDetail
      : matched
        ? {
            upbitUuid,
            state: "wait" as const,
            ordType: "price",
            side: matched.side,
            price: "—",
            volume: "—",
            executedVolume: "0",
            intent: {
              intentType: matched.intentType,
              requestedKrw: matched.requestedKrw,
              reasonCode: matched.reasonCode,
            },
            attempts: [
              {
                attemptPublicId: matched.latestAttempt.attemptPublicId,
                attemptNo: matched.latestAttempt.attemptNo,
                status: matched.latestAttempt.status,
                upbitUuid: matched.latestAttempt.upbitUuid,
                nextRetryAt: matched.latestAttempt.nextRetryAt,
                errorCode: matched.latestAttempt.errorCode,
                createdAt: matched.createdAt,
              },
            ],
            fills: [] as { tradeTime: string; price: string; volume: string; fee?: string }[],
          }
        : null;

  if (!detail) {
    return (
      <div className="space-y-6">
        <h1 className="text-xl font-semibold text-text-1">주문 상세</h1>
        <p className="text-text-2">Upbit 주문 ID: {upbitUuid}</p>
        <p className="text-text-3">해당 주문을 찾을 수 없습니다. (mock)</p>
        <Link href="/orders" className="text-cyan hover:underline">
          ← 주문 목록
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-text-1">주문 상세</h1>
        <Link href="/orders" className="text-sm text-cyan hover:underline">
          ← 목록
        </Link>
      </div>
      <p className="font-mono text-sm text-text-2">Upbit 주문 ID: {detail.upbitUuid}</p>

      {/* Upbit 주문 스냅샷 */}
      <section aria-label="Upbit 주문 스냅샷" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">Upbit 주문 스냅샷</h2>
        <div className="mt-3 grid grid-cols-2 gap-4 sm:grid-cols-4">
          <div>
            <p className="text-xs text-text-3">state</p>
            <p className="mt-1 text-text-1">{detail.state}</p>
          </div>
          <div>
            <p className="text-xs text-text-3">ordType</p>
            <p className="mt-1 text-text-1">{detail.ordType}</p>
          </div>
          <div>
            <p className="text-xs text-text-3">side</p>
            <p className="mt-1">
              <SideBadge side={detail.side} />
            </p>
          </div>
          <div>
            <p className="text-xs text-text-3">price</p>
            <p className="mt-1 tabular-nums text-text-1">{detail.price ?? "—"}</p>
          </div>
          <div>
            <p className="text-xs text-text-3">volume</p>
            <p className="mt-1 tabular-nums text-text-1">{detail.volume ?? "—"}</p>
          </div>
          <div>
            <p className="text-xs text-text-3">executedVolume</p>
            <p className="mt-1 tabular-nums text-text-1">{detail.executedVolume ?? "—"}</p>
          </div>
        </div>
      </section>

      {/* 관련 Intent */}
      {detail.intent && (
        <section aria-label="관련 주문 의도" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
          <h2 className="text-sm font-medium text-text-2">관련 주문 의도</h2>
          <div className="mt-3 grid grid-cols-2 gap-4 sm:grid-cols-4">
            <div>
              <p className="text-xs text-text-3">intentType</p>
              <p className="mt-1">
                <IntentTypeBadge intentType={detail.intent.intentType} />
              </p>
            </div>
            <div>
              <p className="text-xs text-text-3">requestedKrw</p>
              <p className="mt-1 tabular-nums text-text-1">
                {detail.intent.requestedKrw ? formatKrw(detail.intent.requestedKrw) : "—"}
              </p>
            </div>
            <div>
              <p className="text-xs text-text-3">reasonCode</p>
              <p className="mt-1 text-text-1">{detail.intent.reasonCode ?? "—"}</p>
            </div>
          </div>
        </section>
      )}

      {/* Attempt 타임라인 */}
      <section aria-label="시도 타임라인" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">시도 타임라인</h2>
        <div className="mt-3 space-y-3">
          {detail.attempts.map((a) => (
            <div
              key={a.attemptPublicId}
              className="flex items-center gap-4 rounded border border-thin border-borderSubtle bg-bg1 p-3"
            >
              <span className="tabular-nums text-text-3">#{a.attemptNo}</span>
              <StatusChip
                tone={getAttemptStatusTone(a.status)}
                label={
                  a.status === "THROTTLED" && a.nextRetryAt
                    ? `429 ~${formatIso(a.nextRetryAt)}`
                    : a.status
                }
              />
              <span className="text-xs text-text-3">{formatIso(a.createdAt)}</span>
              {a.nextRetryAt && (
                <span className="text-xs text-yellow">next_retry_at: {formatIso(a.nextRetryAt)}</span>
              )}
              {a.errorCode && <span className="text-xs text-red">{a.errorCode}</span>}
            </div>
          ))}
        </div>
      </section>

      {/* Fill(체결) */}
      <section aria-label="체결 내역" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">체결 내역</h2>
        {detail.fills.length > 0 ? (
          <div className="mt-3 overflow-x-auto">
            <table className="w-full border-collapse text-sm">
              <thead>
                <tr className="border-b border-divider text-left text-text-3">
                  <th className="py-2 pr-4">체결 시각</th>
                  <th className="py-2 pr-4">가격</th>
                  <th className="py-2 pr-4">수량</th>
                  <th className="py-2 pr-4">수수료</th>
                </tr>
              </thead>
              <tbody className="text-text-2">
                {detail.fills.map((f, i) => (
                  <tr key={i} className="border-b border-divider">
                    <td className="py-2 pr-4 tabular-nums">{formatIso(f.tradeTime)}</td>
                    <td className="py-2 pr-4 tabular-nums">{formatKrw(f.price)}</td>
                    <td className="py-2 pr-4 tabular-nums">{f.volume}</td>
                    <td className="py-2 pr-4 tabular-nums">{f.fee ? formatKrw(f.fee) : "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="mt-2 text-sm text-text-3">체결 없음</p>
        )}
      </section>
    </div>
  );
}
