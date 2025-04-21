'use client';

import { useState, useEffect } from 'react';
import { UpbitAccount, AccountSummary } from '@/types/upbit';
import { useRouter } from 'next/navigation';
import { memberApi } from '@/api/member';
import { useAuth } from '@/hooks/useAuth';

const formatNumber = (num?: number) => {
  if (num === undefined) return '0';
  return num.toLocaleString();
};

export default function Dashboard() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [account, setAccount] = useState<UpbitAccount | null>(null);
  const [summary, setSummary] = useState<AccountSummary | null>(null);
  const { isAuthenticated, token } = useAuth();

  useEffect(() => {
    const checkUpbitConnection = async () => {
      if (!isAuthenticated) {
        return;
      }

      try {
        const memberInfo = await memberApi.getMemberInfo();
        if (!memberInfo.success) {
          throw new Error('사용자 정보를 가져오는데 실패했습니다.');
        }

        console.log('업비트 연동 상태 확인: memberInfo', memberInfo);

        if (!memberInfo.data?.isUpbitConnected) {
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
  }, [router, isAuthenticated, token]);

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