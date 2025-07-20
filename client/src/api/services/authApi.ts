import { SECURE_API_BASE_URL } from '../config';

// API_BASE_URL 로깅
console.log('Auth API Base URL:', SECURE_API_BASE_URL);

export const authApi = {
    kakaoLogin: (): void => {
        try {
            // URL 구성 전에 환경변수 값 확인
            console.log('SECURE_API_BASE_URL:', SECURE_API_BASE_URL);
            console.log('process.env.NEXT_PUBLIC_SECURE_API_HOST:', process.env.NEXT_PUBLIC_SECURE_API_HOST);
            console.log('process.env.NEXT_PUBLIC_API_HOST:', process.env.NEXT_PUBLIC_API_HOST);

            const loginUrl = `${SECURE_API_BASE_URL}/login/kakao`;
            console.log('최종 카카오 로그인 URL:', loginUrl);
            
            // URL 객체를 사용하여 프로토콜 강제 지정
            const secureUrl = new URL(loginUrl);
            secureUrl.protocol = 'https:';
            
            console.log('보안 처리된 최종 URL:', secureUrl.toString());
            window.location.href = secureUrl.toString();
        } catch (error) {
            console.error('카카오 로그인 에러:', error);
            throw error;
        }
    },

    naverLogin: (): void => {
        try {
            const loginUrl = `${SECURE_API_BASE_URL}/login/naver`;
            console.log('네이버 로그인 URL:', loginUrl);
            
            const secureUrl = new URL(loginUrl);
            secureUrl.protocol = 'https:';
            
            window.location.href = secureUrl.toString();
        } catch (error) {
            console.error('네이버 로그인 에러:', error);
            throw error;
        }
    },
} as const; 