import { z } from 'zod';

// Request Types
export const InquiryRequest = z.object({
    content: z.string(),
});

export type InquiryRequest = z.infer<typeof InquiryRequest>; 