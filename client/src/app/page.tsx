'use client';

import { FaChartLine, FaRobot, FaHistory, FaBriefcase } from 'react-icons/fa';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';

export default function Home() {
  const router = useRouter();
  const [isVisible, setIsVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const { isAuthenticated } = useAuth(false);

  useEffect(() => {
    setIsVisible(true);
  }, []);

  const handleStart = () => {
    if (isAuthenticated) {
      router.push('/dashboard');
    } else {
      router.push('/login');
    }
  };

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="relative min-h-screen flex items-center justify-center bg-gradient-to-b from-navy-500 to-navy-700 px-4">
        <div className="w-full max-w-7xl mx-auto">
          <div className="text-center space-y-6 sm:space-y-8">
            <div className={`space-y-3 sm:space-y-4 ${isVisible ? 'animate-fade-in-down' : 'opacity-0'}`}>
              <h3 className="text-base sm:text-lg md:text-2xl font-medium text-navy-100 tracking-wide">
                비트코인 자동 트레이딩 시스템
              </h3>
              <h1 className="text-4xl sm:text-5xl md:text-6xl lg:text-7xl xl:text-8xl font-bold text-white tracking-tight font-logo">
                everbit
              </h1>
              <div className="w-16 sm:w-24 h-1 bg-navy-300 mx-auto"></div>
            </div>
            <p className={`text-lg sm:text-xl md:text-2xl text-navy-100 max-w-2xl mx-auto leading-relaxed px-4 ${isVisible ? 'animate-fade-in' : 'opacity-0'}`}>
              Upbit API 기반의 안전하고 효율적인 자동 트레이딩
            </p>
            <div className={`pt-2 sm:pt-4 ${isVisible ? 'animate-fade-in-up' : 'opacity-0'}`}>
              <button
                onClick={handleStart}
                className="w-full sm:w-auto px-8 sm:px-16 py-3 sm:py-4 bg-white text-navy-900 rounded-lg font-medium hover:bg-navy-100 focus:outline-none transition-colors shadow-lg hover:shadow-xl"
              >
                {isAuthenticated ? '대시보드로 이동' : '시작하기'}
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-12 sm:py-16 md:py-24 bg-white px-4">
        <div className="max-w-7xl mx-auto">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 md:gap-8">
            {/* Feature 1 */}
            <div className="bg-navy-50 p-4 sm:p-6 rounded-lg">
              <FaChartLine className="text-navy-500 text-3xl sm:text-4xl mb-3 sm:mb-4" />
              <h3 className="text-lg sm:text-xl font-bold text-navy-800 mb-2">실시간 시장 분석</h3>
              <p className="text-sm sm:text-base text-navy-600">최신 시장 데이터를 기반으로 한 정확한 분석</p>
            </div>

            {/* Feature 2 */}
            <div className="bg-navy-50 p-4 sm:p-6 rounded-lg">
              <FaRobot className="text-navy-500 text-3xl sm:text-4xl mb-3 sm:mb-4" />
              <h3 className="text-lg sm:text-xl font-bold text-navy-800 mb-2">자동화된 트레이딩</h3>
              <p className="text-sm sm:text-base text-navy-600">24시간 자동으로 작동하는 트레이딩 시스템</p>
            </div>

            {/* Feature 3 */}
            <div className="bg-navy-50 p-4 sm:p-6 rounded-lg">
              <FaHistory className="text-navy-500 text-3xl sm:text-4xl mb-3 sm:mb-4" />
              <h3 className="text-lg sm:text-xl font-bold text-navy-800 mb-2">거래 내역 추적</h3>
              <p className="text-sm sm:text-base text-navy-600">모든 거래 내역을 실시간으로 확인 가능</p>
            </div>

            {/* Feature 4 */}
            <div className="bg-navy-50 p-4 sm:p-6 rounded-lg">
              <FaBriefcase className="text-navy-500 text-3xl sm:text-4xl mb-3 sm:mb-4" />
              <h3 className="text-lg sm:text-xl font-bold text-navy-800 mb-2">포트폴리오 관리</h3>
              <p className="text-sm sm:text-base text-navy-600">자산 현황과 수익률을 한눈에 파악</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
} 