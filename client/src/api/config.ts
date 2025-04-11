import axios from 'axios';

const API_HOST = process.env.NEXT_PUBLIC_API_HOST || 'localhost';

export const apiClient = axios.create({
    baseURL: `https://${API_HOST}/api`,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true
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