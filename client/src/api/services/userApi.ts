import { apiClient } from '../lib/apiClient';
import { EmailRequest, UpbitApiKeysRequest, UpbitApiKeysResponse, UserResponse } from '../types';

export const userApi = {
    getCurrentUser: async (): Promise<UserResponse> => {
        return apiClient.get<UserResponse>('/users/me');
    },

    registerUpbitApiKeys: async (request: UpbitApiKeysRequest): Promise<UserResponse> => {
        return apiClient.patch<UserResponse>('/users/me/upbit-keys', request);
    },

    getUpbitApiKeys: async (): Promise<UpbitApiKeysResponse> => {
        return apiClient.get<UpbitApiKeysResponse>('/users/me/upbit-keys');
    },

    updateEmail: async (request: EmailRequest): Promise<UserResponse> => {
        return apiClient.patch<UserResponse>('/users/me/email', request);
    },

    deleteUser: async (): Promise<void> => {
        return apiClient.delete('/users/me');
    },

    toggleBotActive: async (): Promise<UserResponse> => {
        return apiClient.patch<UserResponse>('/users/me/bot-active');
    },
} as const; 