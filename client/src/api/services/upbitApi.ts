import { apiClient } from '../lib/apiClient';
import { UpbitAccount } from '../types';

export const upbitApi = {
    getAccounts: async (): Promise<UpbitAccount[]> => {
        return apiClient.get<UpbitAccount[]>('/accounts/me');
    },
} as const; 