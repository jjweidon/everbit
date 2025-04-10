import { apiClient } from './config';
import { AxiosError } from 'axios';

export const loginApi = {
    kakaoLogin: async (): Promise<void> => {
        try {
            console.log('카카오 로그인 요청 시작');
            const response = await apiClient.get('/login/kakao');
            console.log('카카오 로그인 응답:', {
                status: response.status,
                statusText: response.statusText,
                headers: response.headers,
                data: response.data
            });
            
            if (response.data.data) {
                console.log('카카오 로그인 URL로 리다이렉트:', response.data.data);
                window.location.href = response.data.data;
            } else {
                console.error('카카오 로그인 URL이 없습니다:', response.data);
            }
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