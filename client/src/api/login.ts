import { apiClient } from './config';

export const loginApi = {
    kakaoLogin: async (): Promise<void> => {
        const response = await apiClient.get('/api/login/kakao');
        if (response.data.redirectUrl) {
            window.location.href = response.data.redirectUrl;
        }
    }
}; 