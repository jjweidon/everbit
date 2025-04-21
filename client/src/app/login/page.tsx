'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import Image from 'next/image';
import { loginApi } from '@/api/login';
import { FaChartLine, FaRobot, FaHistory, FaBriefcase } from 'react-icons/fa';
import KakaoIcon from '@/components/icons/KakaoIcon';
import NaverIcon from '@/components/icons/NaverIcon';
import { useEffect } from 'react';
import { useAuthStore, getExpirationFromToken } from '@/store/authStore';

export default function Login() {
  const router = useRouter();
  const { token, isTokenValid } = useAuthStore();

  useEffect(() => {
    // 디버깅: 페이지 로드 시 URL과 헤더 정보 출력
    if (typeof window !== 'undefined') {
      console.log('-------------- 로그인 페이지 로드 디버깅 --------------');
      
      // 로컬 스토리지 상태 확인
      console.log('로컬 스토리지 상태:');
      console.log('  AuthStatus:', localStorage.getItem('AuthStatus'));
      console.log('  Authorization:', localStorage.getItem('Authorization') ? '토큰 있음' : '토큰 없음');
      
      // Zustand 스토어 상태 확인
      console.log('Zustand 상태:');
      console.log('  token:', token ? '토큰 있음' : '토큰 없음');
      console.log('  isTokenValid:', token ? isTokenValid() : false);
      
      console.log('----------------------------------------------------');
    }
  }, [token, isTokenValid]);

  useEffect(() => {
    // 클라이언트 사이드에서만 실행
    if (typeof window !== 'undefined') {
      const authStatus = localStorage.getItem('AuthStatus');
      const authToken = localStorage.getItem('Authorization');
      
      // 로그인 후 토큰이 있는지 확인
      if (authToken) {
        console.log('----- 로그인 성공 감지: 토큰 확인 -----');
        console.log('토큰 존재 확인: 토큰 있음');
        console.log('authStatus:', authStatus);
        
        // 인증 상태가 'loggedIn'이거나 유효한 인증 토큰이 있는 경우 대시보드로 리디렉션
        if ((authStatus && authStatus === 'loggedIn') || (authToken && authToken.length > 0)) {
          console.log('인증된 사용자 감지, 대시보드로 이동합니다.');
          // 지연 시간을 두어 리디렉션 실행
          setTimeout(() => {
            router.push('/dashboard');
          }, 100);
        }
      } else {
        console.log('로그인 페이지 로드 - authStatus:', authStatus);
        console.log('로그인 페이지 로드 - authToken: 토큰 없음');
      }
    }
  }, [router]);

  const handleKakaoLogin = async () => {
    console.log('==== 카카오 로그인 핸들러 시작 ====');
    try {
      console.log('카카오 로그인 드가자 로그인 페이지에서');
      
      // 브라우저 환경인지 확인
      if (typeof window === 'undefined') {
        console.log('브라우저 환경이 아닙니다. 로그인을 진행할 수 없습니다.');
        return;
      }
      
      // 현재 상태 확인
      if (token && isTokenValid()) {
        console.log('이미 인증된 사용자, 대시보드로 이동');
        setTimeout(() => {
          router.push('/dashboard');
        }, 100);
        return;
      }
      
      console.log('=== 카카오 로그인 API 호출 직전 ===');
      await loginApi.kakaoLogin();
      console.log('=== 카카오 로그인 API 호출 이후 ==='); // 이 로그는 리디렉션으로 인해 출력되지 않을 수 있음
    } catch (error) {
      console.error('카카오 로그인 에러:', error);
    }
    console.log('==== 카카오 로그인 핸들러 종료 ===='); // 예외가 발생하지 않는 한 이 로그가 출력되어야 함
  };

  const handleNaverLogin = async () => {
    console.log('==== 네이버 로그인 핸들러 시작 ====');
    try {
      // 브라우저 환경인지 확인
      if (typeof window === 'undefined') {
        console.log('브라우저 환경이 아닙니다. 로그인을 진행할 수 없습니다.');
        return;
      }
      
      // 현재 상태 확인
      if (token && isTokenValid()) {
        console.log('이미 인증된 사용자, 대시보드로 이동');
        setTimeout(() => {
          router.push('/dashboard');
        }, 100);
        return;
      }
      
      console.log('=== 네이버 로그인 API 호출 직전 ===');
      await loginApi.naverLogin();
      console.log('=== 네이버 로그인 API 호출 이후 ==='); // 이 로그는 리디렉션으로 인해 출력되지 않을 수 있음
    } catch (error) {
      console.error('네이버 로그인 에러:', error);
    }
    console.log('==== 네이버 로그인 핸들러 종료 ===='); // 예외가 발생하지 않는 한 이 로그가 출력되어야 함
  };

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
              <button
                onClick={handleKakaoLogin}
                className="w-full sm:w-64 flex items-center justify-center space-x-2 px-4 py-3 bg-[#FEE500] text-[#3C1E1E] rounded-lg font-medium hover:bg-[#FFEB34]/90 transition-colors"
              >
                <KakaoIcon />
                <span>카카오 로그인</span>
              </button>
              <button
                onClick={handleNaverLogin}
                className="w-full sm:w-64 flex items-center justify-center space-x-2 px-4 py-3 bg-[#03CF5D] text-white rounded-lg font-medium hover:bg-[#14D96B]/90 transition-colors"
              >
                <NaverIcon />
                <span>네이버 로그인</span>
              </button>
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