import { UpbitAccount } from '@/types/upbit';
import { apiClient } from './apiClient';
import { UserResponse } from './dto/UserResponse';

export const upbitApi = {
    saveUpbitApiKeys: async (accessKey: string, secretKey: string): Promise<UserResponse> => {
        return apiClient.post<UserResponse>('/accounts', { accessKey, secretKey });
    },

    getAccounts: async (): Promise<UpbitAccount[]> => {
        return apiClient.get<UpbitAccount[]>('/accounts/me');
    }
}; 