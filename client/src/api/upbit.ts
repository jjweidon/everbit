import { UpbitAccount } from '@/types/upbit';
import { apiClient } from './config';

export const upbitApi = {
    getAccounts: async (): Promise<UpbitAccount[]> => {
        const response = await apiClient.get<UpbitAccount[]>('/upbit/accounts');
        return response.data;
    }
}; 