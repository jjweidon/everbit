/**
 * 백테스트 — 목록 + 실행 폼
 * docs/ui/everbit_ui_impl_spec.md §5.8
 * Mock 데이터 사용, 실 API 호출 금지
 */
import Link from "next/link";
import { StatusChip, TagBadge } from "@/components/ui";
import { mockBacktestList } from "@/lib/mocks/backtests";
import type { BacktestJobStatus } from "@/types/api-contracts";

function formatIso(iso: string) {
  return new Date(iso).toLocaleString("ko-KR");
}

function getStatusTone(s: BacktestJobStatus): "green" | "red" | "yellow" | "cyan" | "neutral" {
  const map: Record<BacktestJobStatus, "green" | "red" | "yellow" | "cyan" | "neutral"> = {
    QUEUED: "neutral",
    RUNNING: "cyan",
    DONE: "green",
    FAILED: "red",
  };
  return map[s];
}

export default function BacktestsPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">백테스트</h1>

      <section aria-label="실행 폼" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">실행 폼</h2>
        <div className="mt-3 grid grid-cols-2 gap-4 text-sm text-text-3 sm:grid-cols-4">
          <span>markets (multi)</span>
          <span>timeframes (multi)</span>
          <span>period (from/to)</span>
          <span>initialCapital</span>
          <span>fee / slippage</span>
        </div>
        <p className="mt-2 text-xs text-text-3">(API 연동 후 구현)</p>
      </section>

      <section aria-label="백테스트 목록" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">목록</h2>
        <div className="mt-3 overflow-x-auto">
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="border-b border-divider text-left text-text-3">
                <th className="py-2 pr-4">작업 ID</th>
                <th className="py-2 pr-4">상태</th>
                <th className="py-2 pr-4">생성 시각</th>
                <th className="py-2 pr-4">마켓/타임프레임</th>
              </tr>
            </thead>
            <tbody className="text-text-2">
              {mockBacktestList.map((j) => (
                <tr key={j.jobPublicId} className="border-b border-divider">
                  <td className="py-2 pr-4">
                    <Link
                      href={`/backtests/${j.jobPublicId}`}
                      className="font-mono text-cyan hover:underline"
                    >
                      {j.jobPublicId}
                    </Link>
                  </td>
                  <td className="py-2 pr-4">
                    <StatusChip tone={getStatusTone(j.status)} label={j.status} />
                  </td>
                  <td className="py-2 pr-4 tabular-nums">{formatIso(j.createdAt)}</td>
                  <td className="py-2 pr-4">
                    <span className="flex flex-wrap gap-1.5">
                      {j.markets.map((m) => (
                        <TagBadge key={m}>{m}</TagBadge>
                      ))}
                      <span className="text-text-3">/ {j.timeframes.join(", ")}</span>
                    </span>
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
