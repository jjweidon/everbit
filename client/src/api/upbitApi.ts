import { UpbitAccount } from '@/types/upbit';
import { apiClient } from './apiClient';
import { UserResponse } from './dto/UserResponse';
import { UpbitKeyRequest } from './dto/UpbitKeyRequest';

export const upbitApi = {
    saveUpbitApiKeys: async (request: UpbitKeyRequest): Promise<UserResponse> => {
        return apiClient.post<UserResponse>('/accounts', request);
    },

    getAccounts: async (): Promise<UpbitAccount[]> => {
        return apiClient.get<UpbitAccount[]>('/accounts/me');
    },
};
