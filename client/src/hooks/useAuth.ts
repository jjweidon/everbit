import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';

/**
 * 인증이 필요한 페이지에서 사용하는 훅
 * 인증되지 않은 사용자를 로그인 페이지로 리디렉션
 * @param requireAuth - 인증이 필요한지 여부 (기본값: true)
 */
export function useAuth(requireAuth = true) {
  const router = useRouter();
  const { token, isTokenValid } = useAuthStore();

  useEffect(() => {
    // 인증이 필요하지 않은 페이지(예: 메인 페이지, 로그인 페이지)에서는 체크 건너뜀
    if (!requireAuth) {
      return;
    }

    // 인증 상태 확인
    const checkAuth = () => {
      if (!token || !isTokenValid()) {
        console.log('인증되지 않은 상태: 로그인 페이지로 이동');
        router.push('/login');
      }
    };

    checkAuth();
  }, [requireAuth, router, token, isTokenValid]);

  return { 
    isAuthenticated: !!token && isTokenValid(),
    token,
  };
} 