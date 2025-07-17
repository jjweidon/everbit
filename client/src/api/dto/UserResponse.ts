import { z } from 'zod';

const UserResponse = z.object({
    userId: z.string(),
    username: z.string(),
    nickname: z.string(),
    image: z.string(),
    isUpbitConnected: z.boolean(),
});

export type UserResponse = z.infer<typeof UserResponse>;
