'use client';

import { useState, useEffect } from 'react';
import { UpbitAccount, AccountSummary } from '@/types/upbit';
import { useRouter } from 'next/navigation';
import { memberApi } from '@/api/member';

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

  useEffect(() => {
    const checkAuthAndUpbitConnection = async () => {
      try {
        console.log('로그인 상태 확인 필요');
        // 1. 로그인 상태 확인
        const authStatus = localStorage.getItem('AuthStatus');
        console.log('authStatus', authStatus);
        if (!authStatus || authStatus !== 'authenticated') {
          // 로그인 페이지로 이동
          console.log('인증 필요해서 로그인 페이지로 이동');
          router.push('/login');
          return;
        }

        // 2. 사용자 정보 조회
        const memberInfo = await memberApi.getMemberInfo();
        if (!memberInfo.success) {
          throw new Error('사용자 정보를 가져오는데 실패했습니다.');
        }

        console.log('업비트 연동 상태 확인 필요: memberInfo', memberInfo);

        // 3. 업비트 연동 상태 확인
        if (!memberInfo.data?.isUpbitConnected) {
          // 업비트 키가 등록되지 않은 경우
          router.push('/upbit-api-key');
          return;
        }

        // 4. 모든 조건이 충족되면 대시보드 표시
        setIsLoading(false);
      } catch (err) {
        setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
        setIsLoading(false);
      }
    };

    checkAuthAndUpbitConnection();
  }, [router]);

  if (isLoading || error) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-navy-500 to-navy-700">
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