import { API_BASE_URL } from './config';

export const loginApi = {
    kakaoLogin: (): void => {
        try {
            console.log(API_BASE_URL + '/login/kakao');
            window.location.href = `${API_BASE_URL}/login/kakao`;
        } catch (error) {
            console.error('카카오 로그인 에러:', error);
            throw error;
        }
    },
};
