import { z } from 'zod';

// Request Types

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