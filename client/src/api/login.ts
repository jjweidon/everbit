import { apiClient } from './config';
import { ApiResponse, LoginResponse } from './dto/ApiResponse';

export const loginApi = {
    kakaoLogin: (): void => {
        try {
            console.log('카카오 로그인 요청 시작');
            const apiHost = process.env.NEXT_PUBLIC_API_HOST || '';
            window.location.href = `${apiHost}/login/kakao`;
        } catch (error) {
            console.error('카카오 로그인 에러:', error);
            throw error;
        }
    },
    naverLogin: async (): Promise<void> => {
        try {
            console.log('네이버 로그인 요청 시작');
            const apiHost = process.env.NEXT_PUBLIC_API_HOST || '';
            window.location.href = `${apiHost}/login/naver`;
        } catch (error) {
            console.error('네이버 로그인 에러:', error);
            throw error;
        }
    }
};