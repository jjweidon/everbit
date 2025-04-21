import axios from 'axios';

const API_HOST = process.env.NEXT_PUBLIC_API_HOST || 'api.everbit.kr';

export const apiClient = axios.create({
    baseURL: `${API_HOST}`,
    headers: {
        'Content-Type': 'application/json',
    }
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