import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface AuthState {
  token: string | null;
  status: 'authenticated' | 'unauthenticated' | 'loading';
  expiresAt: number | null;
  
  // 액션
  setToken: (token: string) => void;
  login: (token: string, expiresAt: number) => void;
  logout: () => void;
  isTokenValid: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      status: 'unauthenticated',
      expiresAt: null,
      
      setToken: (token: string) => set({ token }),
      
      login: (token: string, expiresAt: number) => {
        set({ 
          token, 
          status: 'authenticated', 
          expiresAt 
        });
        
        // 로컬스토리지 동기화 (레거시 코드 지원)
        if (typeof window !== 'undefined') {
          localStorage.setItem('Authorization', token);
          localStorage.setItem('AuthStatus', 'loggedIn');
        }
      },
      
      logout: () => {
        set({ 
          token: null, 
          status: 'unauthenticated', 
          expiresAt: null 
        });
        
        // 로컬스토리지 정리 (레거시 코드 지원)
        if (typeof window !== 'undefined') {
          localStorage.removeItem('Authorization');
          localStorage.removeItem('AuthStatus');
          localStorage.removeItem('isLoggingIn');
          localStorage.removeItem('loginTimestamp');
        }
      },
      
      isTokenValid: () => {
        const { token, expiresAt } = get();
        
        if (!token || !expiresAt) {
          return false;
        }
        
        // 현재 시간과 만료 시간 비교
        return Date.now() < expiresAt;
      }
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => localStorage)
    }
  )
);

// JWT 토큰에서 만료 시간을 추출하는 유틸리티 함수
export const getExpirationFromToken = (token: string): number | null => {
  try {
    const payload = token.split('.')[1];
    const decoded = JSON.parse(atob(payload));
    
    if (decoded.exp) {
      // JWT의 exp는 초 단위, JavaScript의 Date.now()는 밀리초 단위
      return decoded.exp * 1000;
    }
    return null;
  } catch (error) {
    console.error('토큰 디코딩 오류:', error);
    return null;
  }
}; 