import axios from 'axios';
import { useAuthStore } from '@/store/authStore';

const API_HOST = process.env.NEXT_PUBLIC_API_HOST || 'api.everbit.kr';

export const apiClient = axios.create({
    baseURL: `${API_HOST}`,
    headers: {
        'Content-Type': 'application/json',
    }
});

// 요청 인터셉터 추가
apiClient.interceptors.request.use(
    (config) => {
        // 브라우저 환경인 경우에만 실행
        if (typeof window !== 'undefined') {
            const token = useAuthStore.getState().token;
            
            // 토큰이 있으면 헤더에 추가
            if (token) {
                config.headers['Authorization'] = token;
            }
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 응답 인터셉터 추가
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        // 401 Unauthorized 오류 처리
        if (error.response && error.response.status === 401) {
            console.error('인증이 만료되었습니다. 다시 로그인해주세요.');
            
            // 브라우저 환경인 경우에만 실행
            if (typeof window !== 'undefined') {
                // 로그아웃 처리
                useAuthStore.getState().logout();
                
                // 현재 페이지가 로그인 페이지가 아닌 경우에만 리디렉션
                if (!window.location.pathname.includes('/login')) {
                    window.location.href = '/login';
                }
            }
        }
        
        console.error('API Error:', error);
        return Promise.reject(error);
    }
); 