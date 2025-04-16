import { apiClient } from './config';
import { AxiosError } from 'axios';

interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

interface MemberInfo {
    id: number;
    name: string;
    email: string;
    isUpbitConnected: boolean;
}

export const memberApi = {
    getMemberInfo: async (): Promise<ApiResponse<MemberInfo>> => {
        try {
            const response = await apiClient.get<ApiResponse<MemberInfo>>('/api/members/me');
            return response.data;
        } catch (error) {
            throw error;
        }
    }
};