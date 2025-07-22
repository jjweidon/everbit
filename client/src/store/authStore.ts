// stores/useAuthStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { userApi } from '@/api/services';
import { UserResponse } from '@/api/types';
import { HttpClientFactory } from '@/api/lib/HttpClientFactory';

interface AuthState {
    user: UserResponse | null;
    isAuthenticated: boolean;
    logout: () => void;
    fetchUser: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => {
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

                    // 모든 쿠키 제거
                    document.cookie.split(';').forEach(function (c) {
                        document.cookie = c
                            .replace(/^ +/, '')
                            .replace(/=.*/, '=;expires=' + new Date().toUTCString() + ';path=/');
                    });
                }
            };

            // 로그아웃 핸들러 등록
            HttpClientFactory.setLogoutHandler(logout);

            return {
                user: null,
                isAuthenticated: false,
                logout,

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
            };
        },
        {
            name: 'auth-storage',
        }
    )
);
