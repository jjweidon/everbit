import { secureApiClient } from '../lib/secureApiClient';
import { UpbitAccount } from '../types';

export const upbitApi = {
    getAccounts: async (): Promise<UpbitAccount[]> => {
        return secureApiClient.get<UpbitAccount[]>('/accounts/me');
    },
} as const; 