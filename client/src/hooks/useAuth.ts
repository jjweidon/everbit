import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { authStore } from '@/store/authStore';

/**
 * 인증 관련 React 훅
 * - 라우팅 처리
 * - 인증이 필요한 페이지 보호
 * - authStore와 React 컴포넌트 연동
 */
export const useAuth = (options: { required?: boolean } = {}) => {
  const { required = false } = options;
  const router = useRouter();
  const auth = authStore();  // Zustand store를 구독

  useEffect(() => {
    const checkAuth = async () => {
      if (!auth.isAuthenticated) {
        try {
          await auth.fetchUser();
        } catch (error) {
          if (required) {
            router.push('/');
          }
        }
      }
    };
    
    checkAuth();
  }, [required, router, auth.isAuthenticated, auth.fetchUser]);

  return {
    ...auth,  // authStore의 모든 상태와 메서드
    
    // 라우팅 유틸리티
    redirectToLogin: () => router.push('/'),
    redirectToDashboard: () => router.push('/dashboard')
  };
};

/**
 * 인증이 필요한 페이지를 위한 훅
 */
export const useRequireAuth = () => {
  return useAuth({ required: true });
}; 