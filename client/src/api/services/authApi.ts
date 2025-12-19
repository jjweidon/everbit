import { SECURE_API_BASE_URL } from '../config';
import axios from 'axios';
import { tokenStorage } from '@/utils/tokenStorage';

// API_BASE_URL 로깅
console.log('Auth API Base URL:', SECURE_API_BASE_URL);

export const authApi = {
    kakaoLogin: (): void => {
        try {
            const loginUrl = `${SECURE_API_BASE_URL}/login/kakao`;
            console.log('카카오 로그인 URL:', loginUrl);
            window.location.href = loginUrl;
        } catch (error) {
            console.error('카카오 로그인 에러:', error);
            throw error;
        }
    },

    naverLogin: (): void => {
        try {
            const loginUrl = `${SECURE_API_BASE_URL}/login/naver`;
            console.log('네이버 로그인 URL:', loginUrl);
            window.location.href = loginUrl;
        } catch (error) {
            console.error('네이버 로그인 에러:', error);
            throw error;
        }
    },

    refreshToken: async (): Promise<void> => {
        try {
            const response = await axios.post(
                `${SECURE_API_BASE_URL}/auth/refresh`,
                {},
                {
                    withCredentials: true,
                }
            );
            
            // 응답에서 새로운 Access 토큰 추출하여 로컬 스토리지에 저장
            if (response.data?.data?.accessToken) {
                tokenStorage.setAccessToken(response.data.data.accessToken);
            }
            
            console.log('토큰 갱신 성공');
            return response.data;
        } catch (error) {
            console.error('토큰 갱신 실패:', error);
            throw error;
        }
    },
} as const; 