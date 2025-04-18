import { apiClient } from './config';
import { ApiResponse, LoginResponse } from './dto/ApiResponse';

export const loginApi = {
    kakaoLogin: async (): Promise<void> => {
        try {
            console.log('카카오 로그인 요청 시작');
            const response = await apiClient.get<ApiResponse<LoginResponse>>('/login/kakao');
            if (response.data.success && response.data.data.redirectUrl) {
                window.location.href = response.data.data.redirectUrl;
            }
        } catch (error) {
            console.error('카카오 로그인 에러:', error);
            throw error;
        }
    },
    naverLogin: async (): Promise<void> => {
        try {
            console.log('네이버 로그인 요청 시작');
            const response = await apiClient.get<ApiResponse<LoginResponse>>('/login/naver');
            if (response.data.success && response.data.data.redirectUrl) {
                window.location.href = response.data.data.redirectUrl;
            }
        } catch (error) {
            console.error('네이버 로그인 에러:', error);
            throw error;
        }
    }
};