'use client';

import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { loginApi } from '@/api/login';
import { FaChartLine, FaRobot, FaHistory, FaBriefcase } from 'react-icons/fa';

export default function Login() {
  const router = useRouter();

  const handleKakaoLogin = async () => {
    try {
      await loginApi.kakaoLogin();
    } catch (error) {
      console.error('카카오 로그인 에러:', error);
    }
  };

  return (
    <div className="min-h-screen bg-navy-500 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-2xl">
        <div className="flex justify-center mb-8">
          <div className="w-24 h-24 bg-white rounded-full flex items-center justify-center">
            <FaChartLine className="h-12 w-12 text-navy-500" />
          </div>
        </div>
        <h2 className="mt-6 text-center text-5xl font-extrabold text-white font-logo">
          everbit
        </h2>
        <p className="mt-4 text-center text-xl text-navy-100">
          업비트 API를 활용한 안전하고 효율적인 비트코인 자동 트레이딩
        </p>
      </div>

      <div className="mt-12 sm:mx-auto sm:w-full sm:max-w-2xl">
        <div className="bg-white py-12 px-8 shadow-xl sm:rounded-lg">
          <div className="space-y-8">
            <div className="text-center">
              <h3 className="text-2xl font-bold text-navy-900 mb-2">시작하기</h3>
              <p className="text-navy-600">카카오 계정으로 간편하게 로그인하세요</p>
            </div>
            
            <div className="flex justify-center">
              <button
                onClick={handleKakaoLogin}
                className="w-full max-w-md flex justify-center items-center"
              >
                <Image
                  src="/images/kakao_login_button.png"
                  alt="카카오 로그인"
                  width={300}
                  height={45}
                  className="cursor-pointer"
                />
              </button>
            </div>

            <div className="mt-8 grid grid-cols-3 gap-4">
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