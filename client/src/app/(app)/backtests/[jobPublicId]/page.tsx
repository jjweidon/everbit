/**
 * 백테스트 상세 — /backtests/[jobPublicId]
 * docs/ui/everbit_ui_impl_spec.md §5.8
 * Mock 데이터 사용, 실 API 호출 금지
 */
import Link from "next/link";
import { StatusChip, TagBadge } from "@/components/ui";
import { mockBacktestDetail, mockBacktestList } from "@/lib/mocks/backtests";
import type { BacktestDetail } from "@/types/api-contracts";

function formatIso(iso: string) {
  return new Date(iso).toLocaleString("ko-KR");
}

interface BacktestDetailPageProps {
  params: { jobPublicId: string };
}

export default async function BacktestDetailPage({ params }: BacktestDetailPageProps) {
  const { jobPublicId } = params;

  const detail =
    jobPublicId === mockBacktestDetail.jobPublicId
      ? mockBacktestDetail
      : mockBacktestList.find((j) => j.jobPublicId === jobPublicId) ?? null;

  if (!detail) {
    return (
      <div className="space-y-6">
        <h1 className="text-xl font-semibold text-text-1">백테스트 상세</h1>
        <p className="text-text-2">작업 ID: {jobPublicId}</p>
        <p className="text-text-3">해당 작업을 찾을 수 없습니다. (mock)</p>
        <Link href="/backtests" className="text-cyan hover:underline">
          ← 목록
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-text-1">백테스트 상세</h1>
        <Link href="/backtests" className="text-sm text-cyan hover:underline">
          ← 목록
        </Link>
      </div>
      <p className="font-mono text-sm text-text-2">{detail.jobPublicId}</p>

      <section aria-label="상태" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">상태</h2>
        <div className="mt-3 flex flex-wrap gap-4">
          <StatusChip tone={detail.status === "DONE" ? "green" : "neutral"} label={detail.status} />
          <span className="text-sm text-text-3">생성: {formatIso(detail.createdAt)}</span>
          <span className="text-sm text-text-3">마켓: </span>
          <span className="inline-flex flex-wrap gap-1.5">
            {detail.markets.map((m) => (
              <TagBadge key={m}>{m}</TagBadge>
            ))}
          </span>
          <span className="text-sm text-text-3">타임프레임: {detail.timeframes.join(", ")}</span>
        </div>
      </section>

      {"metrics" in detail && detail.metrics ? (
        <section aria-label="메트릭" className="rounded-lg border border-border bg-bg2 p-4">
          <h2 className="text-sm font-medium text-text-2">메트릭</h2>
          <div className="mt-3 grid grid-cols-2 gap-4 sm:grid-cols-4">
            {(detail as BacktestDetail).metrics?.cagr != null && (
              <div>
                <p className="text-xs text-text-3">CAGR</p>
                <p className="mt-1 tabular-nums text-text-1">
                  {((detail as BacktestDetail).metrics!.cagr! * 100).toFixed(2)}%
                </p>
              </div>
            )}
            {(detail as BacktestDetail).metrics?.mdd != null && (
              <div>
                <p className="text-xs text-text-3">MDD</p>
                <p className="mt-1 tabular-nums text-text-1">
                  {((detail as BacktestDetail).metrics!.mdd! * 100).toFixed(2)}%
                </p>
              </div>
            )}
            {(detail as BacktestDetail).metrics?.winRate != null && (
              <div>
                <p className="text-xs text-text-3">승률</p>
                <p className="mt-1 tabular-nums text-text-1">
                  {((detail as BacktestDetail).metrics!.winRate! * 100).toFixed(1)}%
                </p>
              </div>
            )}
            {(detail as BacktestDetail).metrics?.profitFactor != null && (
              <div>
                <p className="text-xs text-text-3">Profit Factor</p>
                <p className="mt-1 tabular-nums text-text-1">
                  {(detail as BacktestDetail).metrics!.profitFactor!.toFixed(2)}
                </p>
              </div>
            )}
          </div>
        </section>
      ) : null}

      {"requestJson" in detail && detail.requestJson ? (
        <section aria-label="요청 파라미터" className="rounded-lg border border-border bg-bg2 p-4">
          <h2 className="text-sm font-medium text-text-2">요청 파라미터</h2>
          <pre className="mt-3 overflow-x-auto rounded border border-border bg-bg1 p-3 text-xs text-text-2">
            {JSON.stringify((detail as BacktestDetail).requestJson ?? {}, null, 2)}
          </pre>
        </section>
      ) : null}
    </div>
  );
}
