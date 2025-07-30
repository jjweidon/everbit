import { apiClient } from '../lib/apiClient';
import { EmailRequest, UpbitApiKeysRequest, UpbitApiKeysResponse, UserResponse, BotSettingResponse, BotSettingRequest } from '../types';

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

    getBotSetting: async (): Promise<BotSettingResponse> => {
        return apiClient.get<BotSettingResponse>('/users/me/bot-setting');
    },

    updateBotSetting: async (request: BotSettingRequest): Promise<BotSettingResponse> => {
        return apiClient.put<BotSettingResponse>('/users/me/bot-setting', request);
    },
} as const; 