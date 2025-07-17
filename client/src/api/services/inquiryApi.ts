import { apiClient } from '../lib/apiClient';
import { InquiryRequest } from '../types';

export const inquiryApi = {
    submitInquiry: async (request: InquiryRequest): Promise<void> => {
        return apiClient.post('/inquiries', request);
    },
} as const; 