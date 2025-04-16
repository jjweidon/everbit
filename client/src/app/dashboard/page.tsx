'use client';

import Link from 'next/link';
import { useState, useEffect } from 'react';
import { UpbitAccount, AccountSummary } from '@/types/upbit';
import { FaChartLine, FaRobot, FaHistory, FaBriefcase } from 'react-icons/fa';

export default function Dashboard() {
  const [account, setAccount] = useState<UpbitAccount | null>(null);
  const [summary, setSummary] = useState<AccountSummary | null>(null);

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <div className="bg-navy-500 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex justify-between items-center">
            <h1 className="text-2xl font-bold">대시보드</h1>
            <Link
              href="/upbit-api-key"
              className="px-4 py-2 bg-white text-navy-700 rounded-md hover:bg-navy-50"
            >
              API 키 설정
            </Link>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats Grid */}
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
          {/* Total Balance */}
          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <FaBriefcase className="h-6 w-6 text-navy-500" />
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">
                      총 자산
                    </dt>
                    <dd className="flex items-baseline">
                      <div className="text-2xl font-semibold text-gray-900">
                        {summary?.totalBalance.toLocaleString()} KRW
                      </div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>

          {/* Available Balance */}
          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <FaChartLine className="h-6 w-6 text-navy-500" />
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">
                      사용 가능 금액
                    </dt>
                    <dd className="flex items-baseline">
                      <div className="text-2xl font-semibold text-gray-900">
                        {summary?.availableBalance.toLocaleString()} KRW
                      </div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>

          {/* Total Profit */}
          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <FaRobot className="h-6 w-6 text-navy-500" />
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">
                      총 수익
                    </dt>
                    <dd className="flex items-baseline">
                      <div className="text-2xl font-semibold text-gray-900">
                        {summary?.totalProfit.toLocaleString()} KRW
                      </div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>

          {/* Profit Rate */}
          <div className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <FaHistory className="h-6 w-6 text-navy-500" />
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">
                      수익률
                    </dt>
                    <dd className="flex items-baseline">
                      <div className="text-2xl font-semibold text-gray-900">
                        {summary?.profitRate.toFixed(2)}%
                      </div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Trading Section */}
        <div className="mt-8 grid grid-cols-1 gap-8 lg:grid-cols-2">
          {/* Trading Status */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-medium text-gray-900 mb-4">
              트레이딩 상태
            </h2>
            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-500">현재 상태</span>
                <span className="px-2 py-1 text-sm font-medium rounded-full bg-green-100 text-green-800">
                  활성화
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-500">마지막 거래</span>
                <span className="text-sm text-gray-900">2024-04-16 15:30:45</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-500">다음 거래 예정</span>
                <span className="text-sm text-gray-900">2024-04-16 16:00:00</span>
              </div>
            </div>
          </div>

          {/* Trading Settings */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-medium text-gray-900 mb-4">
              트레이딩 설정
            </h2>
            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-500">거래 전략</span>
                <span className="text-sm text-gray-900">RSI + MACD</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-gray-500">거래 간격</span>
                <span className="text-sm text-gray-900">30분</span>
              </div>
              <div className="flex justify-between items-center">
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