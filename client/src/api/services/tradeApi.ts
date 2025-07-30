import { apiClient } from "../lib/apiClient";
import { TradeResponse } from "../types/trade";

export const tradeApi = {
    getTrades: async (): Promise<TradeResponse[]> => {
        return apiClient.get<TradeResponse[]>("/trades");
    },
};