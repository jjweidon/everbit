export type Market = "BTC" | "ETH" | "SOL" | "DOGE" | "USDT" | "STRIKE" | "XRP" | "PENGU" | "ARDR" | "STRAX" | "ENS" | "AERGO";
export type Strategy = "STOCH_RSI" | "RSI_BB" | "EMA_MOMENTUM" | "BB_MOMENTUM" | "GOLDEN_CROSS" | "ENSEMBLE";
export type Type = "매수" | "매도";
export type Status = "대기" | "관찰" | "완료" | "취소";

export interface TradeResponse {
    tradeId: string;
    orderId: string;
    market: Market;
    strategy: Strategy;
    type: Type;
    price: number;
    amount: number;
    totalPrice: number;
    status: Status;
    updatedAt: Date;
}