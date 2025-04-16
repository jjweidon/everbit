'use client';

import Link from 'next/link';
import { useState, useEffect } from 'react';
import { UpbitAccount, AccountSummary } from '@/types/upbit';
import { useRouter } from 'next/navigation';
import { loginApi } from '@/api/login';
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
        // 1. 로그인 상태 확인
        const authStatus = localStorage.getItem('AuthStatus');
        if (!authStatus || authStatus !== 'authenticated') {
          // 로그인 API 호출
          await loginApi.kakaoLogin();
        }

        // 2. 사용자 정보 조회
        const memberInfo = await memberApi.getMemberInfo();
        if (!memberInfo.success) {
          throw new Error('사용자 정보를 가져오는데 실패했습니다.');
        }

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

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-navy-500 mx-auto"></div>
          <p className="mt-4 text-navy-600">로딩 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-500">{error}</p>
          <button
            onClick={() => router.push('/')}
            className="mt-4 px-4 py-2 bg-navy-500 text-white rounded-lg hover:bg-navy-600"
          >
            홈으로 돌아가기
          </button>
        </div>
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