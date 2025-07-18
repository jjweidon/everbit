import { API_BASE_URL } from '../config';

// API_BASE_URL 로깅
console.log('Auth API Base URL:', API_BASE_URL);

export const authApi = {
    kakaoLogin: (): void => {
        try {
            const loginUrl = `${API_BASE_URL}/login/kakao`;
            console.log('카카오 로그인 URL:', loginUrl);
            window.location.href = loginUrl;
        } catch (error) {
            console.error('카카오 로그인 에러:', error);
            throw error;
        }
    },

    naverLogin: (): void => {
        try {
            const loginUrl = `${API_BASE_URL}/login/naver`;
            console.log('네이버 로그인 URL:', loginUrl);
            window.location.href = loginUrl;
        } catch (error) {
            console.error('네이버 로그인 에러:', error);
            throw error;
        }
    },
} as const; 