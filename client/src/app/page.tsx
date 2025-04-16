'use client';

import { FaChartLine, FaRobot, FaHistory, FaBriefcase } from 'react-icons/fa';
import Link from 'next/link';

export default function Home() {
  return (
    <div className="min-h-screen bg-white">
      {/* Hero Section */}
      <div className="relative bg-navy-500 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24">
          <div className="text-center">
            <h1 className="text-4xl font-bold tracking-tight sm:text-5xl md:text-6xl">
              everbit
            </h1>
            <p className="mt-6 text-xl text-navy-100 max-w-3xl mx-auto">
              업비트 API를 활용한 안전하고 효율적인 비트코인 자동 트레이딩
            </p>
            <div className="mt-10">
              <Link
                href="/dashboard"
                className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-navy-700 bg-white hover:bg-navy-50"
              >
                대시보드 시작하기
              </Link>
            </div>
          </div>
        </div>
      </div>

      {/* Features Section */}
      <div className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <h2 className="text-3xl font-extrabold text-navy-900 sm:text-4xl">
              주요 기능
            </h2>
            <p className="mt-4 text-lg text-navy-600">
              업비트 API를 활용한 다양한 트레이딩 기능
            </p>
          </div>

          <div className="mt-20 grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-4">
            {/* Feature 1 */}
            <div className="pt-6">
              <div className="flow-root bg-navy-50 rounded-lg px-6 pb-8">
                <div className="-mt-6">
                  <div className="inline-flex items-center justify-center p-3 bg-navy-500 rounded-md shadow-lg">
                    <FaChartLine className="h-6 w-6 text-white" />
                  </div>
                  <h3 className="mt-8 text-lg font-medium text-navy-900 tracking-tight">
                    실시간 차트 분석
                  </h3>
                  <p className="mt-5 text-base text-navy-600">
                    실시간으로 업비트 시장 데이터를 분석하여 최적의 매매 시점을 포착합니다.
                  </p>
                </div>
              </div>
            </div>

            {/* Feature 2 */}
            <div className="pt-6">
              <div className="flow-root bg-navy-50 rounded-lg px-6 pb-8">
                <div className="-mt-6">
                  <div className="inline-flex items-center justify-center p-3 bg-navy-500 rounded-md shadow-lg">
                    <FaRobot className="h-6 w-6 text-white" />
                  </div>
                  <h3 className="mt-8 text-lg font-medium text-navy-900 tracking-tight">
                    자동 매매 시스템
                  </h3>
                  <p className="mt-5 text-base text-navy-600">
                    설정한 전략에 따라 자동으로 매매를 실행하여 수익을 극대화합니다.
                  </p>
                </div>
              </div>
            </div>

            {/* Feature 3 */}
            <div className="pt-6">
              <div className="flow-root bg-navy-50 rounded-lg px-6 pb-8">
                <div className="-mt-6">
                  <div className="inline-flex items-center justify-center p-3 bg-navy-500 rounded-md shadow-lg">
                    <FaHistory className="h-6 w-6 text-white" />
                  </div>
                  <h3 className="mt-8 text-lg font-medium text-navy-900 tracking-tight">
                    거래 내역 관리
                  </h3>
                  <p className="mt-5 text-base text-navy-600">
                    모든 거래 내역을 자동으로 기록하고 분석하여 투자 성과를 추적합니다.
                  </p>
                </div>
              </div>
            </div>

            {/* Feature 4 */}
            <div className="pt-6">
              <div className="flow-root bg-navy-50 rounded-lg px-6 pb-8">
                <div className="-mt-6">
                  <div className="inline-flex items-center justify-center p-3 bg-navy-500 rounded-md shadow-lg">
                    <FaBriefcase className="h-6 w-6 text-white" />
                  </div>
                  <h3 className="mt-8 text-lg font-medium text-navy-900 tracking-tight">
                    포트폴리오 관리
                  </h3>
                  <p className="mt-5 text-base text-navy-600">
                    보유 자산과 수익률을 실시간으로 모니터링하여 투자 전략을 최적화합니다.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="bg-navy-50">
        <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:py-16 lg:px-8 lg:flex lg:items-center lg:justify-between">
          <h2 className="text-3xl font-extrabold tracking-tight text-navy-900 sm:text-4xl">
            <span className="block">지금 바로 시작하세요</span>
            <span className="block text-navy-500">업비트 API 키만 있으면 됩니다.</span>
          </h2>
          <div className="mt-8 flex lg:mt-0 lg:flex-shrink-0">
            <div className="inline-flex rounded-md shadow">
              <Link
                href="/dashboard"
                className="inline-flex items-center justify-center px-5 py-3 border border-transparent text-base font-medium rounded-md text-white bg-navy-500 hover:bg-navy-600"
              >
                시작하기
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 