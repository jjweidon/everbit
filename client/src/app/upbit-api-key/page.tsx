'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import UpbitApiKeyForm from '@/components/UpbitApiKeyForm';

export default function UpbitApiKeyPage() {
    const [error, setError] = useState('');
    const router = useRouter();

    const handleSubmit = async (accessKey: string, secretKey: string) => {
        try {
            const response = await fetch('/api/members/upbit-keys', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ accessKey, secretKey }),
            });

            if (response.ok) {
                router.push('/dashboard');
            } else {
                const errorData = await response.json();
                if (response.status === 400 && errorData === "인증된 사용자가 아닙니다.") {
                    setError("로그인이 필요합니다. 로그인 페이지로 이동합니다.");
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
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
                    <div className="flex justify-between items-center">
                        <h1 className="text-2xl font-bold">API 키 설정</h1>
                        <button
                            onClick={() => router.back()}
                            className="px-4 py-2 bg-white text-navy-700 rounded-md hover:bg-navy-50"
                        >
                            뒤로 가기
                        </button>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {error && (
                    <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded relative" role="alert">
                        <span className="block sm:inline">{error}</span>
                    </div>
                )}

                <div className="bg-white shadow rounded-lg p-6">
                    <h2 className="text-lg font-medium text-navy-900 mb-4">
                        업비트 API 키 등록
                    </h2>
                    <p className="text-navy-600 mb-6">
                        업비트에서 발급받은 API 키를 등록하여 서비스를 이용할 수 있습니다.
                        API 키는 안전하게 보관되며, 읽기 전용 권한만 사용합니다.
                    </p>

                    <UpbitApiKeyForm onSubmit={handleSubmit} />
                </div>
            </div>
        </div>
    );
} 