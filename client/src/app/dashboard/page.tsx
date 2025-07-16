'use client';

import { useState, useEffect } from 'react';
import { UpbitAccount, AccountSummary } from '@/types/upbit';
import { useRouter } from 'next/navigation';
import { userApi } from '@/api/userApi';
import { useAuthStore } from '@/store/authStore';

const formatNumber = (num?: number) => {
  if (num === undefined) return '0';
  return num.toLocaleString();
};

export default function Dashboard() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const { isAuthenticated } = useAuthStore();

  useEffect(() => {
    const checkUpbitConnection = async () => {
      if (!isAuthenticated) {
        console.log('로그인 상태가 없습니다.');
        return;
      }

      try {
        const userInfo = await userApi.getCurrentUser();
        console.log('업비트 연동 상태 확인: userInfo', userInfo);
        if (!userInfo.isUpbitConnected) {
          router.push('/upbit-api-key');
          return;
        }

        setIsLoading(false);
      } catch (err) {
        setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
        setIsLoading(false);
      }
    };

    checkUpbitConnection();
  }, [router, isAuthenticated]);

  if (isLoading || error) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-navy-500 to-navy-700">
        {error && (
          <div className="flex items-center justify-center h-screen">
            <div className="text-white bg-red-600 px-6 py-4 rounded-lg">
              <p>{error}</p>
            </div>
          </div>
        )}
        {isLoading && (
          <div className="flex items-center justify-center h-screen">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-white"></div>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-navy-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-3xl font-bold text-navy-800 mb-8">대시보드</h1>
        {/* 대시보드 컨텐츠 */}
      </div>
    </div>
  );
} 