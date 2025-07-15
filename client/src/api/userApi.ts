import { apiClient } from './apiClient';
import { UserResponse } from './dto/UserResponse';

export const userApi = {

    getCurrentUser: async (): Promise<UserResponse> => {
        return apiClient.get<UserResponse>('/users/me');
    },

    saveUpbitApiKeys: async (apiKey: string, secretKey: string): Promise<UserResponse> => {
        return apiClient.post<UserResponse>('/users/upbit-keys', { apiKey, secretKey });
    }
};