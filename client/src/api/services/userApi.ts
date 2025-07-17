import { apiClient } from '../lib/apiClient';
import { EmailRequest, UserResponse } from '../types';

export const userApi = {
    getCurrentUser: async (): Promise<UserResponse> => {
        return apiClient.get<UserResponse>('/users/me');
    },

    updateEmail: async (request: EmailRequest): Promise<UserResponse> => {
        return apiClient.patch<UserResponse>('/users/me/email', request);
    },

    deleteUser: async (): Promise<void> => {
        return apiClient.delete('/users/me');
    },
} as const; 