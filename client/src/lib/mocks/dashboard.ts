import type { DashboardSummary } from "@/types/api-contracts";

export const mockDashboardSummary: DashboardSummary = {
  accountEnabled: true,
  strategyKey: "EXTREME_FLIP",
  strategyEnabled: true,
  wsStatus: "CONNECTED",
  lastReconcileAt: "2026-03-06T02:30:00.000Z",
  lastErrorAt: "2026-03-06T01:15:00.000Z",
  risk: {
    throttled429Count24h: 2,
    blocked418Until: undefined,
    unknownAttempts24h: 1,
    suspendedMarkets: ["KRW-BTC"],
  },
  equity: {
    equityKrw: "12500000",
    realizedPnlKrw: "450000",
    unrealizedPnlKrw: "-32000",
  },
};
