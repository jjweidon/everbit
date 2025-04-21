import { create } from 'zustand';
import { persist } from 'zustand/middleware';

// JWT 토큰에서 만료 시간을 추출하는 유틸리티 함수
export const getExpirationFromToken = (token: string): number | null => {
  try {
    const payload = token.split('.')[1];
    const decoded = JSON.parse(atob(payload));
    
    if (decoded.exp) {
      return decoded.exp * 1000;
    }
    return null;
  } catch (error) {
    console.error('토큰 디코딩 오류:', error);
    return null;
  }
};

// 인증 상태 타입 정의
interface AuthState {
  // 상태
  isLoggedIn: boolean;
  token: string | null;
  expiresAt: number | null;
  userId: string | null;
  userName: string | null;
  userNickname: string | null;
  
  // 액션
  login: (tokenData: { token: string, userId?: string, userName?: string, userNickname?: string }) => void;
  logout: () => void;
  isTokenValid: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // 초기 상태
      isLoggedIn: false,
      token: null,
      expiresAt: null,
      userId: null,
      userName: null,
      userNickname: null,
      
      // 로그인 액션
      login: (tokenData) => {
        const { token, userId = null, userName = null, userNickname = null } = tokenData;
        const expiresAt = getExpirationFromToken(token);
        
        set({ 
          isLoggedIn: true,
          token,
          expiresAt,
          userId,
          userName,
          userNickname
        });
        
        // 로컬스토리지 동기화 (레거시 코드 지원)
        if (typeof window !== 'undefined') {
          localStorage.setItem('Authorization', token);
          localStorage.setItem('AuthStatus', 'loggedIn');
        }
      },
      
      // 로그아웃 액션
      logout: () => {
        set({ 
          isLoggedIn: false,
          token: null, 
          expiresAt: null,
          userId: null,
          userName: null,
          userNickname: null
        });
        
        // 로컬스토리지 정리 (레거시 코드 지원)
        if (typeof window !== 'undefined') {
          localStorage.removeItem('Authorization');
          localStorage.removeItem('AuthStatus');
          localStorage.removeItem('isLoggingIn');
          localStorage.removeItem('loginTimestamp');
        }
      },
      
      // 토큰 유효성 검사
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
      name: 'auth-storage', // 스토리지 이름
      partialize: (state) => ({
        isLoggedIn: state.isLoggedIn,
        token: state.token,
        expiresAt: state.expiresAt,
        userId: state.userId,
        userName: state.userName,
        userNickname: state.userNickname
      })
    }
  )
); 