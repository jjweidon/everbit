import { SECURE_API_BASE_URL } from '../config';

export const authApi = {
    kakaoLogin: (): void => {
        try {
            window.location.href = `${SECURE_API_BASE_URL}/login/kakao`;
        } catch (error) {
            console.error('카카오 로그인 에러:', error);
            throw error;
        }
    },

    naverLogin: (): void => {
        try {
            window.location.href = `${SECURE_API_BASE_URL}/login/naver`;
        } catch (error) {
            console.error('네이버 로그인 에러:', error);
            throw error;
        }
    },
} as const; 