import { apiClient } from './config';
import { AxiosError } from 'axios';

export const loginApi = {
    kakaoLogin: async (): Promise<void> => {
        try {
            console.log('카카오 로그인 요청 시작');
            
            // 브라우저를 통해 직접 로그인 URL로 이동
            window.location.href = 'https://api.everbit.kr/api/oauth2/authorization/kakao';
        } catch (error) {
            const axiosError = error as AxiosError;
            console.error('카카오 로그인 에러 상세:', {
                message: axiosError.message,
                response: axiosError.response?.data,
                status: axiosError.response?.status,
                headers: axiosError.response?.headers
            });
            throw error;
        }
    }
};