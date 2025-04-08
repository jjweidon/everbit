import axios from 'axios';

const API_HOST = process.env.NEXT_PUBLIC_API_HOST || 'localhost';
const API_PORT = process.env.NEXT_PUBLIC_API_PORT || '8080';

export const apiClient = axios.create({
    baseURL: `https://${API_HOST}:${API_PORT}/api`,
    headers: {
        'Content-Type': 'application/json',
    },
});

// 응답 인터셉터 추가
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        // 에러 처리
        console.error('API Error:', error);
        return Promise.reject(error);
    }
); 