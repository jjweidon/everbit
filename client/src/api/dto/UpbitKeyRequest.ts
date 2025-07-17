import { z } from 'zod';

export const UpbitKeyRequest = z.object({
    accessKey: z.string(),
    secretKey: z.string(),
});

export type UpbitKeyRequest = z.infer<typeof UpbitKeyRequest>;
