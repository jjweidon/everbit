export type Strategy = "BOLLINGER_MEAN_REVERSION" | "BB_MOMENTUM" | "EMA_MOMENTUM" | "ENSEMBLE" | "ENHANCED_ENSEMBLE" | "LOSS_MANAGEMENT";
export type Type = "매수" | "매도";
export type Status = "대기" | "관찰" | "완료" | "취소";

export interface TradeResponse {
    tradeId: string;
    orderId: string;
    market: string;
    strategy: Strategy;
    type: Type;
    price: number;
    amount: number;
    totalPrice: number;
    status: Status;
    updatedAt: Date;
}

export interface StrategyResponse {
    name: string;
    value: string;
    description: string;
}

export interface MarketResponse {
    market: string;
    description: string;
}