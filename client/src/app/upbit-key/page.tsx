'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { upbitApi } from '@/api/upbitApi';

export default function UpbitApiKeyPage() {
    const router = useRouter();
    const [accessKey, setAccessKey] = useState('');
    const [secretKey, setSecretKey] = useState('');
    const [error, setError] = useState('');
    const [validationErrors, setValidationErrors] = useState({
        accessKey: '',
        secretKey: ''
    });
    const [isLoading, setIsLoading] = useState(false);

    const validateForm = () => {
        const newErrors = {
            accessKey: !accessKey.trim() ? 'Access Key는 필수값입니다.' : '',
            secretKey: !secretKey.trim() ? 'Secret Key는 필수값입니다.' : ''
        };
        setValidationErrors(newErrors);
        return !Object.values(newErrors).some(error => error);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;

        try {
            setIsLoading(true);
            await upbitApi.saveUpbitApiKeys({ accessKey, secretKey });
            router.push('/dashboard');
        } catch (error) {
            console.error('Error:', error);
            setError('API 키 등록에 실패했습니다.');
        } finally {
            setIsLoading(false);
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
                <div className="bg-white shadow rounded-lg p-4 sm:p-6">
                    <h2 className="text-lg sm:text-xl font-medium text-navy-900 mb-3 sm:mb-4">
                        업비트 API 키 등록
                    </h2>
                    <p className="text-sm sm:text-base text-navy-600 mb-4 sm:mb-6">
                        업비트에서 발급받은 API 키를 등록해야 서비스를 이용할 수 있습니다.
                        API 키는 암호화되어 보관되며, 읽기 전용 권한만 사용합니다.
                    </p>

                    <form onSubmit={handleSubmit} className="mt-8 space-y-6">
                        <div className="space-y-4">
                            <div>
                                <label htmlFor="accessKey" className="block text-sm font-medium text-navy-700 mb-1">
                                    Access Key
                                </label>
                                <input
                                    id="accessKey"
                                    type="text"
                                    value={accessKey}
                                    onChange={(e) => {
                                        setAccessKey(e.target.value);
                                        if (validationErrors.accessKey) {
                                            setValidationErrors(prev => ({ ...prev, accessKey: '' }));
                                        }
                                    }}
                                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-navy-500 focus:border-navy-500 outline-none transition-colors ${
                                        validationErrors.accessKey || error ? 'border-red-500' : 'border-navy-300'
                                    }`}
                                    placeholder="Access Key를 입력하세요"
                                />
                                {validationErrors.accessKey && (
                                    <p className="mt-1 text-xs text-red-500">{validationErrors.accessKey}</p>
                                )}
                            </div>
                            <div>
                                <label htmlFor="secretKey" className="block text-sm font-medium text-navy-700 mb-1">
                                    Secret Key
                                </label>
                                <input
                                    id="secretKey"
                                    type="password"
                                    value={secretKey}
                                    onChange={(e) => {
                                        setSecretKey(e.target.value);
                                        if (validationErrors.secretKey) {
                                            setValidationErrors(prev => ({ ...prev, secretKey: '' }));
                                        }
                                    }}
                                    className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-navy-500 focus:border-navy-500 outline-none transition-colors ${
                                        validationErrors.secretKey || error ? 'border-red-500' : 'border-navy-300'
                                    }`}
                                    placeholder="Secret Key를 입력하세요"
                                />
                                {validationErrors.secretKey && (
                                    <p className="mt-1 text-xs text-red-500">{validationErrors.secretKey}</p>
                                )}
                            </div>
                            {error && (
                                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded relative" role="alert">
                                    <span className="block sm:inline">{error}</span>
                                </div>
                            )}
                        </div>
                        <button
                            type="submit"
                            disabled={isLoading}
                            className="w-full px-4 py-3 bg-navy-500 text-white rounded-lg font-medium hover:bg-navy-600 focus:outline-none focus:ring-2 focus:ring-navy-500 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {isLoading ? '처리 중...' : '저장'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
} 