'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { FaChartLine, FaRobot, FaHistory } from 'react-icons/fa';
import LoginButton from '@/components/LoginButton';
import { useAuth } from '@/hooks/useAuth';

export default function Login() {
  const router = useRouter();
  const { isAuthenticated, fetchUser } = useAuth();

  useEffect(() => {
    // 로그인 상태 확인
    const checkLoginStatus = async () => {
      try {
        // 인증 상태 확인 (API 요청)
        await fetchUser();
        
        // 이미 로그인된 경우 대시보드로 리디렉션
        if (isAuthenticated) {
          console.log('이미 로그인된 사용자, 대시보드로 이동');
          router.push('/dashboard');
        }
      } catch (error) {
        console.error('로그인 상태 확인 중 오류:', error);
      }
    };
    
    checkLoginStatus();
  }, [isAuthenticated, router, fetchUser]);

  return (
    <div className="min-h-screen bg-navy-500 flex flex-col justify-center py-8 sm:py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-2xl mx-auto">
        <div className="flex justify-center">
          <div 
            className="rounded-full flex items-center justify-center animate-rotate-scale cursor-pointer"
            onClick={() => router.push('/')}
          >
            <Image
              src="/logos/logo-icon-2d.webp"
              alt="everbit logo"
              width={120}
              height={120}
              className="object-contain"
            />
          </div>
        </div>
        <h2 className="text-center text-4xl sm:text-5xl font-extrabold text-white font-logo mt-4">
          everbit
        </h2>
        <p className="mt-3 sm:mt-4 text-center text-sm sm:text-xl text-navy-100 px-4">
          업비트 API를 활용한 안전하고 효율적인 비트코인 자동 트레이딩
        </p>
      </div>

      <div className="mt-8 sm:mt-12 w-full max-w-2xl mx-auto">
        <div className="bg-white py-8 sm:py-12 px-6 sm:px-8 shadow-xl rounded-lg">
          <div className="space-y-6 sm:space-y-8">
            <div className="text-center">
              <h3 className="text-lg sm:text-2xl font-bold text-navy-900 mb-2">비트코인 자동 매매하기</h3>
              <p className="text-xs sm:text-base text-navy-600">지금 로그인하고 24시간 자동 수익을 만들어 보세요</p>
            </div>
            
            <div className="flex flex-col items-center space-y-3 sm:space-y-4">
              <LoginButton provider="kakao" className="sm:w-64" />
            </div>

            <div className="hidden sm:grid mt-8 grid-cols-3 gap-4">
              <div className="flex justify-center">
                <div className="flex items-center space-x-3">
                  <div className="flex-shrink-0">
                    <FaRobot className="h-6 w-6 text-navy-500" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-navy-900">자동 매매</p>
                    <p className="text-xs text-navy-600">24시간 자동 트레이딩</p>
                  </div>
                </div>
              </div>
              <div className="flex justify-center">
                <div className="flex items-center space-x-3">
                  <div className="flex-shrink-0">
                    <FaHistory className="h-6 w-6 text-navy-500" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-navy-900">실시간 분석</p>
                    <p className="text-xs text-navy-600">최적의 매매 시점 포착</p>
                  </div>
                </div>
              </div>
              <div className="flex justify-center">
                <div className="flex items-center space-x-3">
                  <div className="flex-shrink-0">
                    <FaChartLine className="h-6 w-6 text-navy-500" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-navy-900">차트 분석</p>
                    <p className="text-xs text-navy-600">다양한 기술적 지표</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 