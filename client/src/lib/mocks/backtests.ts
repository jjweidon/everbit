import type { BacktestJobItem, BacktestDetail } from "@/types/api-contracts";

export const mockBacktestList: BacktestJobItem[] = [
  {
    jobPublicId: "01JCBT0001",
    status: "DONE",
    createdAt: "2026-03-05T14:00:00.000Z",
    markets: ["KRW-BTC", "KRW-ETH"],
    timeframes: ["15", "60"],
  },
  {
    jobPublicId: "01JCBT0002",
    status: "RUNNING",
    createdAt: "2026-03-06T02:00:00.000Z",
    markets: ["KRW-BTC", "KRW-ETH", "KRW-XRP"],
    timeframes: ["15", "60", "240"],
  },
  {
    jobPublicId: "01JCBT0003",
    status: "FAILED",
    createdAt: "2026-03-04T10:00:00.000Z",
    markets: ["KRW-BTC"],
    timeframes: ["15"],
  },
];

export const mockBacktestDetail: BacktestDetail = {
  jobPublicId: "01JCBT0001",
  status: "DONE",
  createdAt: "2026-03-05T14:00:00.000Z",
  markets: ["KRW-BTC", "KRW-ETH"],
  timeframes: ["15", "60"],
  metrics: {
    cagr: 0.24,
    mdd: 0.12,
    winRate: 0.58,
    profitFactor: 1.42,
  },
  requestJson: {
    markets: ["KRW-BTC", "KRW-ETH"],
    timeframes: ["15", "60"],
    period: { from: "2025-01-01", to: "2026-03-01" },
    initialCapital: 10000000,
    fee: 0.0005,
    slippage: 0.001,
  },
};
