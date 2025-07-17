// stores/useAuthStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { userApi } from '@/api/userApi';
import { UserResponse } from '@/api/dto/UserResponse';

interface AuthState {
    user: UserResponse | null;
    isAuthenticated: boolean;
    logout: () => void;
    fetchUser: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            user: null,
            isAuthenticated: false,

            logout: () => {
                // 상태 초기화
                set((state) => ({
                    ...state,
                    user: null,
                    isAuthenticated: false,
                }));

                // persist 저장소 초기화
                if (typeof window !== 'undefined') {
                    localStorage.removeItem('auth-storage');

                    // 모든 쿠키 제거
                    document.cookie.split(';').forEach(function (c) {
                        document.cookie = c
                            .replace(/^ +/, '')
                            .replace(/=.*/, '=;expires=' + new Date().toUTCString() + ';path=/');
                    });
                }
            },

            fetchUser: async () => {
                try {
                    console.log('사용자 정보 로드 시도...');

                    const userData = await userApi.getCurrentUser();

                    if (!userData || !userData.userId) {
                        console.error('유효하지 않은 사용자 데이터:', userData);
                        throw new Error('유효하지 않은 사용자 데이터');
                    }

                    console.log('사용자 정보 로드 성공:', userData);
                    set((state) => ({
                        ...state,
                        user: userData,
                        isAuthenticated: true,
                    }));
                } catch (err) {
                    console.error('fetchUser 실패:', err);
                    set((state) => ({
                        ...state,
                        user: null,
                        isAuthenticated: false,
                    }));
                    throw err;
                }
            },
        }),
        {
            name: 'everbit-auth',
            partialize: (state) => ({
                user: state.user,
                isAuthenticated: state.isAuthenticated,
            }),
        }
    )
);
