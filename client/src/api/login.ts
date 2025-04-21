export const loginApi = {
    kakaoLogin: (): void => {
        try {
            console.log('카카오 로그인 요청 시작');
            const apiHost = process.env.NEXT_PUBLIC_API_HOST || '';
            
            // 리디렉션 전 현재 URL 로깅
            console.log('리디렉션 전 현재 URL:', window.location.href);
            console.log('리디렉션 전 쿠키:', document.cookie);
            
            // 리디렉션 URL 로깅
            const redirectUrl = `${apiHost}/login/kakao`;
            console.log('카카오 로그인 리디렉션 URL:', redirectUrl);
            
            // 로컬 스토리지 초기화
            localStorage.removeItem('isLoggingIn');
            
            window.location.href = redirectUrl;
        } catch (error) {
            console.error('카카오 로그인 에러:', error);
            throw error;
        }
    },
    naverLogin: async (): Promise<void> => {
        try {
            console.log('네이버 로그인 요청 시작');
            const apiHost = process.env.NEXT_PUBLIC_API_HOST || '';
            
            // 리디렉션 전 현재 URL 로깅
            console.log('리디렉션 전 현재 URL:', window.location.href);
            console.log('리디렉션 전 쿠키:', document.cookie);
            
            // 리디렉션 URL 로깅
            const redirectUrl = `${apiHost}/login/naver`;
            console.log('네이버 로그인 리디렉션 URL:', redirectUrl);
            
            // 로컬 스토리지 초기화
            localStorage.removeItem('isLoggingIn');
            
            window.location.href = redirectUrl;
        } catch (error) {
            console.error('네이버 로그인 에러:', error);
            throw error;
        }
    }
};