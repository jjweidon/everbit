import type { MarketStatusItem } from "@/types/api-contracts";

export const mockMarketStatusList: MarketStatusItem[] = [
  {
    market: "KRW-BTC",
    enabled: true,
    priority: 1,
    positionStatus: "SUSPENDED",
    lastSignalAt: "2026-03-06T02:00:00.000Z",
    cooldownUntil: "2026-03-06T04:00:00.000Z",
  },
  {
    market: "KRW-ETH",
    enabled: true,
    priority: 2,
    positionStatus: "OPEN",
    lastSignalAt: "2026-03-06T03:30:00.000Z",
  },
  {
    market: "KRW-XRP",
    enabled: true,
    priority: 3,
    positionStatus: "FLAT",
    lastSignalAt: "2026-03-06T01:20:00.000Z",
    cooldownUntil: "2026-03-06T02:20:00.000Z",
  },
  {
    market: "KRW-SOL",
    enabled: false,
    priority: 4,
    positionStatus: "FLAT",
  },
];
