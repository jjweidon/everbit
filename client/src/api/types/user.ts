import { z } from 'zod';

// Response Types
export const User = z.object({
    userId: z.string(),
    username: z.string(),
    nickname: z.string(),
    email: z.string(),
    image: z.string(),
    createdAt: z.string(),
    isUpbitConnected: z.boolean(),
    role: z.string(),
    isBotActive: z.boolean(),
});
export type UserResponse = z.infer<typeof User>;

export const UpbitApiKeys = z.object({
    accessKey: z.string(),
    secretKey: z.string(),
});
export type UpbitApiKeysResponse = z.infer<typeof UpbitApiKeys>;
export type UpbitApiKeysRequest = z.infer<typeof UpbitApiKeys>;

export const BotSetting = z.object({
    botSettingId: z.string(),
    marketList: z.array(z.string()),
    isBuyActive: z.boolean(),
    isSellActive: z.boolean(),
    buyStrategy: z.string(),
    sellStrategy: z.string(),
    buyBaseOrderAmount: z.number(),
    buyMaxOrderAmount: z.number(),
    sellBaseOrderAmount: z.number(),
    sellMaxOrderAmount: z.number(),
    lossThreshold: z.number(),
    profitThreshold: z.number(),
    lossSellRatio: z.number(),
    profitSellRatio: z.number(),
    isLossManagementActive: z.boolean(),
    isProfitTakingActive: z.boolean(),
    isTimeOutSellActive: z.boolean(),
    timeOutSellMinutes: z.number(),
    timeOutSellProfitRatio: z.number(),
});
export type BotSettingResponse = z.infer<typeof BotSetting>;
export type BotSettingRequest = z.infer<typeof BotSetting>;

// Request Types
export const Email = z.object({
    email: z.string(),
});
export type EmailRequest = z.infer<typeof Email>;