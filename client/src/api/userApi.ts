import { apiClient } from './apiClient';
import { UserResponse } from './dto/UserResponse';

export const userApi = {
    getCurrentUser: async (): Promise<UserResponse> => {
        return apiClient.get<UserResponse>('/users/me');
    },
};
