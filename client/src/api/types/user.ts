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

// Request Types
export const Email = z.object({
    email: z.string(),
});
export type EmailRequest = z.infer<typeof Email>;