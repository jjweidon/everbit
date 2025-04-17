import { apiClient } from './config';
import { AxiosError } from 'axios';

interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

export const loginApi = {
    kakaoLogin: async (): Promise<void> => {
        try {
            console.log('카카오 로그인 요청 시작');
            // OAuth2 클라이언트가 자동으로 생성한 URL로 직접 이동
            window.location.href = 'https://api.everbit.kr/api/login/kakao';
        } catch (error) {
            console.error('카카오 로그인 에러:', error);
            throw error;
        }
    },
    naverLogin: async (): Promise<void> => {
        try {
            console.log('네이버 로그인 요청 시작');
            window.location.href = 'https://api.everbit.kr/api/login/naver';
        } catch (error) {
            console.error('네이버 로그인 에러:', error);
            throw error;
        }
    }
};