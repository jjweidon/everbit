'use client';

import Link from 'next/link';
import { useState, useEffect } from 'react';
import { UpbitAccount, AccountSummary } from '@/types/upbit';
import { FaChartLine, FaRobot, FaHistory, FaBriefcase } from 'react-icons/fa';
import { useRouter } from 'next/navigation';

const formatNumber = (num?: number) => {
  if (num === undefined) return '0';
  return num.toLocaleString();
};

export default function Dashboard() {
  const [account, setAccount] = useState<UpbitAccount | null>(null);
  const [summary, setSummary] = useState<AccountSummary | null>(null);

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <div className="bg-navy-500 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 sm:py-6">
          <div className="flex flex-col sm:flex-row justify-between items-center space-y-4 sm:space-y-0">
            <h1 className="text-xl sm:text-2xl font-bold">대시보드</h1>
            <Link
              href="/upbit-api-key"
              className="w-full sm:w-auto px-4 py-2 bg-white text-navy-700 rounded-md hover:bg-navy-50 text-center sm:text-left"
            >
              API 키 설정
            </Link>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-white p-4 rounded-lg shadow">
            <h3 className="text-gray-500">총 자산</h3>
            <p className="text-2xl font-bold">{formatNumber(summary?.totalBalance)} KRW</p>
          </div>
          <div className="bg-white p-4 rounded-lg shadow">
            <h3 className="text-gray-500">사용 가능한 잔액</h3>
            <p className="text-2xl font-bold">{formatNumber(summary?.availableBalance)} KRW</p>
          </div>
          <div className="bg-white p-4 rounded-lg shadow">
            <h3 className="text-gray-500">총 평가 손익</h3>
            <p className={`text-2xl font-bold ${(summary?.totalProfit ?? 0) >= 0 ? 'text-green-500' : 'text-red-500'}`}>
              {formatNumber(summary?.totalProfit)} KRW
            </p>
          </div>
          <div className="bg-white p-4 rounded-lg shadow">
            <h3 className="text-gray-500">수익률</h3>
            <p className={`text-2xl font-bold ${(summary?.profitRate ?? 0) >= 0 ? 'text-green-500' : 'text-red-500'}`}>
              {(summary?.profitRate ?? 0).toFixed(2)}%
            </p>
          </div>
        </div>

        {/* Trading Section */}
        <div className="mt-6 sm:mt-8 grid grid-cols-1 lg:grid-cols-2 gap-6 sm:gap-8">
          {/* Trading Status */}
          <div className="bg-white shadow rounded-lg p-4 sm:p-6">
            <h2 className="text-base sm:text-lg font-medium text-gray-900 mb-3 sm:mb-4">
              트레이딩 상태
            </h2>
            <div className="space-y-3 sm:space-y-4">
              <div className="flex flex-col sm:flex-row sm:justify-between items-start sm:items-center space-y-1 sm:space-y-0">
                <span className="text-sm text-gray-500">현재 상태</span>
                <span className="px-2 py-1 text-sm font-medium rounded-full bg-green-100 text-green-800">
                  활성화
                </span>
              </div>
              <div className="flex flex-col sm:flex-row sm:justify-between items-start sm:items-center space-y-1 sm:space-y-0">
                <span className="text-sm text-gray-500">마지막 거래</span>
                <span className="text-sm text-gray-900">2024-04-16 15:30:45</span>
              </div>
              <div className="flex flex-col sm:flex-row sm:justify-between items-start sm:items-center space-y-1 sm:space-y-0">
                <span className="text-sm text-gray-500">다음 거래 예정</span>
                <span className="text-sm text-gray-900">2024-04-16 16:00:00</span>
              </div>
            </div>
          </div>

          {/* Trading Settings */}
          <div className="bg-white shadow rounded-lg p-4 sm:p-6">
            <h2 className="text-base sm:text-lg font-medium text-gray-900 mb-3 sm:mb-4">
              트레이딩 설정
            </h2>
            <div className="space-y-3 sm:space-y-4">
              <div className="flex flex-col sm:flex-row sm:justify-between items-start sm:items-center space-y-1 sm:space-y-0">
                <span className="text-sm text-gray-500">거래 전략</span>
                <span className="text-sm text-gray-900">RSI + MACD</span>
              </div>
              <div className="flex flex-col sm:flex-row sm:justify-between items-start sm:items-center space-y-1 sm:space-y-0">
                <span className="text-sm text-gray-500">거래 간격</span>
                <span className="text-sm text-gray-900">30분</span>
              </div>
              <div className="flex flex-col sm:flex-row sm:justify-between items-start sm:items-center space-y-1 sm:space-y-0">
                <span className="text-sm text-gray-500">최대 거래 금액</span>
                <span className="text-sm text-gray-900">100,000 KRW</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 