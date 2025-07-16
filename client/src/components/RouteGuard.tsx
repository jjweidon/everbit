'use client';

import { useEffect } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';

interface RouteGuardProps {
  children: React.ReactNode;
}

const RouteGuard = ({ children }: RouteGuardProps) => {
  const router = useRouter();
  const pathname = usePathname();
  const { fetchUser } = useAuthStore();

  // 공개 경로 목록
  const publicPaths = ['/', '/login', '/docs'];

  useEffect(() => {
    const checkAuth = async () => {
      // 공개 경로인 경우 인증 체크 건너뛰기
      if (publicPaths.includes(pathname)) {
        console.log('RouteGuard: 공개 경로, 인증 체크 건너뛰기', { pathname });
        return;
      }

      try {
        console.log('RouteGuard: 인증 체크 시작', { pathname });
        
        // 항상 최신 사용자 정보를 가져와서 확인
        await fetchUser();
        
        // fetchUser 후 최신 상태 확인
        const currentAuthState = useAuthStore.getState();
        console.log('RouteGuard: 현재 인증 상태', { 
          isAuthenticated: currentAuthState.isAuthenticated,
          isUpbitConnected: currentAuthState.user?.isUpbitConnected,
          pathname 
        });
        
        // 인증되지 않은 경우 루트 페이지로 리다이렉트
        if (!currentAuthState.isAuthenticated) {
          console.log('RouteGuard: 인증되지 않음, 로그인 페이지로 리다이렉트');
          router.replace('/login');
          return;
        }

        // 인증된 상태에서 업비트 연동 체크
        if (
          currentAuthState.user &&
          !currentAuthState.user.isUpbitConnected && 
          !pathname.startsWith('/upbit-key')
        ) {
          console.log('RouteGuard: 업비트 연동 미완료, 업비트 연동으로 리다이렉트');
          router.replace('/upbit-key');
          return;
        }
        
        console.log('RouteGuard: 인증 및 업비트 연동 체크 완료');
      } catch (error) {
        console.error('RouteGuard: 인증 체크 실패:', error);
        // 공개 경로가 아닌 경우에만 로그인 페이지로 리다이렉트
        if (!publicPaths.includes(pathname)) {
          router.replace('/login');
        }
      }
    };

    checkAuth();
  }, [pathname, fetchUser, router]);

  return <>{children}</>;
};

export default RouteGuard; 