'use client';

import { useEffect, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';

interface RouteGuardProps {
    children: React.ReactNode;
}

const RouteGuard = ({ children }: RouteGuardProps) => {
    const router = useRouter();
    const pathname = usePathname();
    const { fetchUser } = useAuthStore();
    const [isAuthorized, setIsAuthorized] = useState(false);

    // 공개 경로 목록
    const publicPaths = ['/', '/login', '/docs'];
    // 관리자 전용 경로 목록
    const adminPaths = ['/admin'];

    // 경로가 publicPaths 중 하나와 일치하거나, 끝에 슬래시만 추가된 경우 true 반환
    const isPublicPath = (path: string): boolean => {
        return publicPaths.some(publicPath => 
            path === publicPath || path === `${publicPath}/`
        );
    };

    useEffect(() => {
        const checkAuth = async () => {
            // 공개 경로인 경우 인증 체크 건너뛰기
            if (isPublicPath(pathname)) {
                console.log('RouteGuard: 공개 경로, 인증 체크 건너뛰기', { pathname });
                setIsAuthorized(true);
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
                    role: currentAuthState.user?.role,
                    pathname,
                });

                // 인증되지 않은 경우 루트 페이지로 리다이렉트
                if (!currentAuthState.isAuthenticated) {
                    console.log('RouteGuard: 인증되지 않음, 로그인 페이지로 리다이렉트');
                    router.replace('/login');
                    return;
                }

                // 관리자 전용 페이지 접근 체크
                if (adminPaths.some(path => pathname.startsWith(path)) && currentAuthState.user?.role !== 'ROLE_ADMIN') {
                    console.log('RouteGuard: 관리자 권한 없음, 대시보드로 리다이렉트');
                    router.replace('/dashboard');
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
                setIsAuthorized(true);
            } catch (error) {
                console.error('RouteGuard: 인증 체크 실패:', error);
                if (!isPublicPath(pathname)) {
                    router.replace('/login');
                }
            }
        };

        setIsAuthorized(false);
        checkAuth();
    }, [pathname, fetchUser, router]);

    // 권한 체크가 완료되고 authorized된 경우에만 children을 렌더링
    return isAuthorized ? <>{children}</> : null;
};

export default RouteGuard;
