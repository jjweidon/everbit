import axios from 'axios';

const API_HOST = process.env.NEXT_PUBLIC_API_HOST || 'api.everbit.kr';

export const apiClient = axios.create({
    baseURL: `https://${API_HOST}/api`,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true
});

// 응답 인터셉터 추가
apiClient.interceptors.response.use(
    (response) => {
        if (response.data.data) {
            console.log('카카오 로그인 URL로 리다이렉트:', response.data.data);
            // 여기서 response.data.data가 http://로 시작하는 URL일 수 있습니다
            window.location.href = response.data.data;
        }
        return response;
    },
    (error) => {
        // 에러 처리
        console.error('API Error:', error);
        return Promise.reject(error);
    }
); 