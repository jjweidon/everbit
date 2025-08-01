import { apiClient } from "../lib/apiClient";
import { TradeResponse, StrategyResponse } from "../types/trade";

export const tradeApi = {
    getTrades: async (): Promise<TradeResponse[]> => {
        return apiClient.get<TradeResponse[]>("/trades");
    },
    
    getStrategies: async (): Promise<StrategyResponse[]> => {
        return apiClient.get<StrategyResponse[]>("/trades/strategies");
    },
};