import type { StrategyConfig } from "@/types/api-contracts";

export const mockStrategyConfig: StrategyConfig = {
  strategyKey: "EXTREME_FLIP",
  configVersion: 3,
  updatedAt: "2026-03-05T10:00:00.000Z",
  configJson: {
    timeframes: ["15", "60"],
    regime: { minStrength: 0.6 },
    entry: { minSignalStrength: 0.7, maxOpenMarkets: 3 },
    exit: { stopLossPct: 0.03, takeProfitPct: 0.05 },
    risk: { minOrderKrw: 50000, maxOrderKrw: 500000 },
    execution: { cooldownMinutes: 60 },
  },
};
