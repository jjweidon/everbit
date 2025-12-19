// stores/useAuthStore.ts
import { create, StateCreator } from 'zustand';
import { persist } from 'zustand/middleware';
import { userApi } from '@/api/services';
import { UserResponse } from '@/api/types';
import { HttpClientFactory } from '@/api/lib/HttpClientFactory';
import { tokenStorage } from '@/utils/tokenStorage';

interface AuthState {
    user: UserResponse | null;
    isAuthenticated: boolean;
    logout: () => void;
    fetchUser: () => Promise<void>;
}

// 메모이제이션을 위해 store 외부에서 함수 정의
const createAuthSlice: StateCreator<AuthState, [], [["zustand/persist", AuthState]]> = (set) => {
    const logout = () => {
        // 상태 초기화
        set((state) => ({
            ...state,
            user: null,
            isAuthenticated: false,
        }));

        // persist 저장소 초기화
        if (typeof window !== 'undefined') {
            localStorage.removeItem('auth-storage');
            
            // Access 토큰 삭제
            tokenStorage.removeAccessToken();

            // 모든 쿠키 제거 (Refresh 토큰 포함)
            document.cookie.split(';').forEach(function (c) {
                document.cookie = c
                    .replace(/^ +/, '')
                    .replace(/=.*/, '=;expires=' + new Date().toUTCString() + ';path=/');
            });
        }
    };

    const fetchUser = async () => {
        try {
            const userData = await userApi.getCurrentUser();

            if (!userData || !userData.userId) {
                throw new Error('유효하지 않은 사용자 데이터');
            }

            console.log('사용자 정보 로드 성공:', userData);
            set((state) => ({
                ...state,
                user: userData,
                isAuthenticated: true,
            }));
        } catch (err) {
            console.error('사용자 정보 로드 실패:', err);
            set((state) => ({
                ...state,
                user: null,
                isAuthenticated: false,
            }));
            throw err;
        }
    };

    // 로그아웃 핸들러 등록
    HttpClientFactory.setLogoutHandler(logout);

    return {
        user: null,
        isAuthenticated: false,
        logout,
        fetchUser,
    };
};

export const useAuthStore = create<AuthState>()(
    persist(
        createAuthSlice,
        {
            name: 'auth-storage',
        }
    )
);
