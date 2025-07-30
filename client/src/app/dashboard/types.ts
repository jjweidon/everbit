export interface OverviewData {
    totalAsset: number;
    profitRate: number;
    realizedProfit: number;
    notifications: ReadonlyArray<Notification>;
}

export interface Notification {
    readonly type: 'error' | 'info' | 'success' | 'warning';
    readonly message: string;
}

export interface PortfolioData {
    coins: ReadonlyArray<CoinPosition>;
}

export interface CoinPosition {
    readonly symbol: string;
    readonly entryPrice: number;
    readonly currentPrice: number;
    readonly profitRate: number;
    readonly amount: number;
}

export interface TradeHistoryData {
    trades: ReadonlyArray<Trade>;
}

export interface Trade {
    readonly time: string;
    readonly symbol: string;
    readonly type: 'entry' | 'exit';
    readonly amount: number;
    readonly profit: number | null;
}

export interface BotSettingsData {
    algorithms: ReadonlyArray<Algorithm>;
    currentSettings: BotSettings;
}

export interface Algorithm {
    readonly id: string;
    readonly name: string;
    readonly description: string;
}

export interface BotSettings {
    readonly botSettingId: string;
    readonly strategy: string;
    readonly marketList: ReadonlyArray<string>;
    readonly baseOrderAmount: number;
    readonly maxOrderAmount: number;
    readonly startTime: string;
    readonly endTime: string;
    readonly candleInterval: string;
    readonly candleCount: number;
}

export interface BacktestData {
    results: {
        readonly cagr: number;
        readonly mdd: number;
        readonly winRate: number;
        readonly profitFactor: number;
    };
}

export type DashboardTab = 'overview' | 'portfolio' | 'history' | 'settings';
