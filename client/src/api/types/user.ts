import { z } from 'zod';

// Response Types
export const UserResponse = z.object({
    userId: z.string(),
    username: z.string(),
    nickname: z.string(),
    email: z.string(),
    image: z.string(),
    createdAt: z.string(),
    isUpbitConnected: z.boolean(),
    role: z.string(),
});

export type UserResponse = z.infer<typeof UserResponse>;

// Request Types
export const EmailRequest = z.object({
    email: z.string(),
});

export type EmailRequest = z.infer<typeof EmailRequest>; 