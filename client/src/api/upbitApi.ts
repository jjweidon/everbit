import { UpbitAccount } from '@/types/upbit';
import { apiClient } from './apiClient';

export const upbitApi = {
    getAccounts: async (): Promise<UpbitAccount[]> => {
        return apiClient.get<UpbitAccount[]>('/upbit/accounts');
    }
}; 