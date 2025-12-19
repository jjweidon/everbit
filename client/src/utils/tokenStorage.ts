/**
 * Access 토큰을 로컬 스토리지에 저장/조회/삭제하는 유틸리티
 */

const ACCESS_TOKEN_KEY = 'access_token';

export const tokenStorage = {
    /**
     * Access 토큰 저장
     */
    setAccessToken: (token: string): void => {
        if (typeof window !== 'undefined') {
            localStorage.setItem(ACCESS_TOKEN_KEY, token);
        }
    },

    /**
     * Access 토큰 조회
     */
    getAccessToken: (): string | null => {
        if (typeof window !== 'undefined') {
            return localStorage.getItem(ACCESS_TOKEN_KEY);
        }
        return null;
    },

    /**
     * Access 토큰 삭제
     */
    removeAccessToken: (): void => {
        if (typeof window !== 'undefined') {
            localStorage.removeItem(ACCESS_TOKEN_KEY);
        }
    },

    /**
     * Access 토큰 존재 여부 확인
     */
    hasAccessToken: (): boolean => {
        return tokenStorage.getAccessToken() !== null;
    },
};

