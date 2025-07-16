'use client';

import * as React from 'react';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { upbitApi } from '@/api/upbitApi';

interface UpbitApiKeyFormProps {
    onSubmit?: (accessKey: string, secretKey: string) => void;
}

export default function UpbitApiKeyForm({ onSubmit }: UpbitApiKeyFormProps) {
    const router = useRouter();
    const [accessKey, setAccessKey] = useState('');
    const [secretKey, setSecretKey] = useState('');
    const [errors, setErrors] = useState({
        accessKey: '',
        secretKey: ''
    });
    const [isLoading, setIsLoading] = useState(false);

    const validateForm = () => {
        const newErrors = {
            accessKey: !accessKey.trim() ? 'Access Key는 필수값입니다.' : '',
            secretKey: !secretKey.trim() ? 'Secret Key는 필수값입니다.' : ''
        };
        setErrors(newErrors);
        return !Object.values(newErrors).some(error => error);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;

        try {
            setIsLoading(true);
            const response = await upbitApi.saveUpbitApiKeys(accessKey, secretKey);
            
            if (response.isUpbitConnected) {
                if (onSubmit) {
                    onSubmit(accessKey, secretKey);
                } else {
                    router.push('/dashboard');
                }
            } else {
                setErrors({
                    accessKey: response.isUpbitConnected ? '' : '업비트 연동에 실패했습니다.',
                    secretKey: response.isUpbitConnected ? '' : '업비트 연동에 실패했습니다.'
                });
            }
        } catch (error) {
            setErrors({
                accessKey: error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.',
                secretKey: error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.'
            });
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="mt-8 space-y-6">
            <div>
                <h2 className="text-2xl font-bold text-navy-800 mb-6">
                    Upbit API 키 설정
                </h2>
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
                                if (errors.accessKey) {
                                    setErrors(prev => ({ ...prev, accessKey: '' }));
                                }
                            }}
                            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-navy-500 focus:border-navy-500 outline-none transition-colors ${
                                errors.accessKey ? 'border-red-500' : 'border-navy-300'
                            }`}
                            placeholder="Access Key를 입력하세요"
                        />
                        {errors.accessKey && (
                            <p className="mt-1 text-xs text-red-500">{errors.accessKey}</p>
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
                                if (errors.secretKey) {
                                    setErrors(prev => ({ ...prev, secretKey: '' }));
                                }
                            }}
                            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-navy-500 focus:border-navy-500 outline-none transition-colors ${
                                errors.secretKey ? 'border-red-500' : 'border-navy-300'
                            }`}
                            placeholder="Secret Key를 입력하세요"
                        />
                        {errors.secretKey && (
                            <p className="mt-1 text-xs text-red-500">{errors.secretKey}</p>
                        )}
                    </div>
                </div>
            </div>
            <button
                type="submit"
                disabled={isLoading}
                className="w-full px-4 py-3 bg-navy-500 text-white rounded-lg font-medium hover:bg-navy-600 focus:outline-none focus:ring-2 focus:ring-navy-500 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
                {isLoading ? '처리 중...' : '저장'}
            </button>
        </form>
    );
} 