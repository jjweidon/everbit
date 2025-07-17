import { apiClient } from '../lib/apiClient';
import { UpbitKeyRequest, UserResponse, UpbitAccount } from '../types';

export const upbitApi = {
    registerUpbitApiKeys: async (request: UpbitKeyRequest): Promise<UserResponse> => {
        return apiClient.post<UserResponse>('/accounts', request);
    },

    getAccounts: async (): Promise<UpbitAccount[]> => {
        return apiClient.get<UpbitAccount[]>('/accounts/me');
    },
} as const; 