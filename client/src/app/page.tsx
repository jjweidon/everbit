'use client';

import { FaChartLine, FaRobot, FaHistory, FaBriefcase } from 'react-icons/fa';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

export default function Home() {
  const router = useRouter();
  const [isVisible, setIsVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    setIsVisible(true);
  }, []);

  const handleStart = () => {
    router.push('/dashboard');
  };

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="relative h-screen flex items-center justify-center bg-gradient-to-b from-navy-500 to-navy-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center space-y-8">
            <div className={`space-y-4 ${isVisible ? 'animate-fade-in-down' : 'opacity-0'}`}>
              <h3 className="text-lg sm:text-xl md:text-2xl font-medium text-navy-100 tracking-wide">
                비트코인 자동 트레이딩 시스템
              </h3>
              <h1 className="text-5xl sm:text-6xl md:text-7xl lg:text-8xl font-bold text-white tracking-tight font-logo">
                everbit
              </h1>
              <div className="w-24 h-1 bg-navy-300 mx-auto"></div>
            </div>
            <p className={`text-xl sm:text-2xl text-navy-100 max-w-2xl mx-auto leading-relaxed ${isVisible ? 'animate-fade-in' : 'opacity-0'}`}>
              Upbit API 기반의 안전하고 효율적인 자동 트레이딩
            </p>
            <div className={`pt-4 ${isVisible ? 'animate-fade-in-up' : 'opacity-0'}`}>
              <button
                onClick={handleStart}
                className="px-8 py-4 bg-navy-500 text-white rounded-lg font-medium hover:bg-navy-600 focus:outline-none focus:ring-2 focus:ring-navy-500 focus:ring-offset-2 transition-colors"
              >
                시작하기
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-16 sm:py-24 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            {/* Feature 1 */}
            <div className="bg-navy-50 p-6 rounded-lg">
              <FaChartLine className="text-navy-500 text-4xl mb-4" />
              <h3 className="text-xl font-bold text-navy-800 mb-2">실시간 시장 분석</h3>
              <p className="text-navy-600">최신 시장 데이터를 기반으로 한 정확한 분석</p>
            </div>

            {/* Feature 2 */}
            <div className="bg-navy-50 p-6 rounded-lg">
              <FaRobot className="text-navy-500 text-4xl mb-4" />
              <h3 className="text-xl font-bold text-navy-800 mb-2">자동화된 트레이딩</h3>
              <p className="text-navy-600">24시간 자동으로 작동하는 트레이딩 시스템</p>
            </div>

            {/* Feature 3 */}
            <div className="bg-navy-50 p-6 rounded-lg">
              <FaHistory className="text-navy-500 text-4xl mb-4" />
              <h3 className="text-xl font-bold text-navy-800 mb-2">거래 내역 추적</h3>
              <p className="text-navy-600">모든 거래 내역을 실시간으로 확인 가능</p>
            </div>

            {/* Feature 4 */}
            <div className="bg-navy-50 p-6 rounded-lg">
              <FaBriefcase className="text-navy-500 text-4xl mb-4" />
              <h3 className="text-xl font-bold text-navy-800 mb-2">포트폴리오 관리</h3>
              <p className="text-navy-600">자산 현황과 수익률을 한눈에 파악</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
} 