'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import UpbitApiKeyForm from '@/components/UpbitApiKeyForm';
import { useRequireAuth } from '@/hooks/useAuth';

export default function UpbitApiKeyPage() {
    const [error, setError] = useState('');
    const router = useRouter();
    // useAuth 훅 사용 - 인증되지 않은 사용자는 로그인 페이지로 리디렉션
    const { isAuthenticated } = useRequireAuth();

    const handleSubmit = async (accessKey: string, secretKey: string) => {
        try {
            // 제출 전 인증 상태 다시 확인
            if (!isAuthenticated) {
                setError("로그인이 필요합니다. 로그인 페이지로 이동합니다.");
                setTimeout(() => {
                    router.push('/login');
                }, 2000);
                return;
            }

            const response = await fetch('/api/members/upbit-keys', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include', // 쿠키에 있는 인증 토큰 포함
                body: JSON.stringify({ accessKey, secretKey }),
            });

            if (response.ok) {
                router.push('/dashboard');
            } else {
                const errorData = await response.json();
                if (response.status === 401) {
                    setError("인증이 만료되었습니다. 로그인 페이지로 이동합니다.");
                    setTimeout(() => {
                        router.push('/login');
                    }, 2000);
                } else {
                    throw new Error('API 키 등록에 실패했습니다.');
                }
            }
        } catch (error) {
            console.error('Error:', error);
            setError('API 키 등록에 실패했습니다.');
        }
    };

    return (
        <div className="min-h-screen bg-white">
            {/* Header */}
            <div className="bg-navy-500 text-white">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 sm:py-6">
                    <div className="flex flex-col sm:flex-row justify-between items-center space-y-4 sm:space-y-0">
                        <h1 className="text-xl sm:text-2xl font-bold">API 키 설정</h1>
                        <button
                            onClick={() => router.back()}
                            className="w-full sm:w-auto px-4 py-2 bg-white text-navy-700 rounded-md hover:bg-navy-50 text-center sm:text-left"
                        >
                            뒤로 가기
                        </button>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
                {error && (
                    <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded relative" role="alert">
                        <span className="block sm:inline">{error}</span>
                    </div>
                )}

                <div className="bg-white shadow rounded-lg p-4 sm:p-6">
                    <h2 className="text-lg sm:text-xl font-medium text-navy-900 mb-3 sm:mb-4">
                        업비트 API 키 등록
                    </h2>
                    <p className="text-sm sm:text-base text-navy-600 mb-4 sm:mb-6">
                        업비트에서 발급받은 API 키를 등록하여 서비스를 이용할 수 있습니다.
                        API 키는 안전하게 보관되며, 읽기 전용 권한만 사용합니다.
                    </p>

                    <UpbitApiKeyForm onSubmit={handleSubmit} />
                </div>
            </div>
        </div>
    );
} 