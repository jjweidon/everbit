import { z } from 'zod';

// Request Types
export const UpbitKeyRequest = z.object({
    accessKey: z.string(),
    secretKey: z.string(),
});

export type UpbitKeyRequest = z.infer<typeof UpbitKeyRequest>;

// Response Types
export const UpbitAccount = z.object({
    currency: z.string(),
    balance: z.string(),
    locked: z.string(),
    avgBuyPrice: z.string(),
    avgBuyPriceModified: z.boolean(),
    unitCurrency: z.string(),
});

export type UpbitAccount = z.infer<typeof UpbitAccount>; 