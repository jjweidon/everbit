import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

// 쿠키에서 값을 가져오는 유틸리티 함수
const getCookie = (name: string): string | null => {
  if (typeof document === 'undefined') return null;
  
  const cookies = document.cookie.split('; ').reduce((acc: Record<string, string>, cookie) => {
    const [cookieName, cookieValue] = cookie.split('=');
    acc[cookieName] = cookieValue;
    return acc;
  }, {});
  
  return cookies[name] || null;
};

interface AuthState {
  token: string | null;
  status: 'authenticated' | 'unauthenticated' | 'loading';
  expiresAt: number | null;
  
  // 액션
  setToken: (token: string) => void;
  login: (token: string, expiresAt: number) => void;
  logout: () => void;
  isTokenValid: () => boolean;
  checkAuthFromCookies: () => boolean;
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
      },
      
      // 쿠키에서 인증 정보를 확인하고 스토어에 저장하는 함수
      checkAuthFromCookies: () => {
        // 쿠키에서 토큰 확인
        const cookieToken = getCookie('Authorization');
        
        if (!cookieToken) {
          return false;
        }
        
        try {
          // 토큰에서 만료 시간 추출
          const expiresAt = getExpirationFromToken(cookieToken);
          
          if (expiresAt && expiresAt > Date.now()) {
            // 유효한 토큰이면 스토어에 저장
            set({
              token: cookieToken,
              status: 'authenticated',
              expiresAt
            });
            
            // 로컬스토리지에도 저장 (일관성 유지)
            if (typeof window !== 'undefined') {
              localStorage.setItem('Authorization', cookieToken);
              localStorage.setItem('AuthStatus', 'loggedIn');
            }
            
            console.log('쿠키에서 유효한 토큰 발견, 인증 상태로 설정');
            return true;
          }
        } catch (e) {
          console.error('쿠키 토큰 확인 중 오류:', e);
        }
        
        return false;
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