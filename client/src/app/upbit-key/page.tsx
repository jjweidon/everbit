'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { upbitApi } from '@/api/services';
import {
    ValidationErrors,
    FormInput,
    UpbitKeyGuide,
    UpbitApiKeyDescription,
    UpbitApiKeyLink,
} from './components';
import MainHeader from '@/components/MainHeader';

const ErrorAlert = ({ message }: { message: string }) => (
    <div
        className="bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 px-4 py-3 rounded relative"
        role="alert"
    >
        <span className="block sm:inline">{message}</span>
    </div>
);

const SubmitButton = ({ isLoading }: { isLoading: boolean }) => (
    <button
        type="submit"
        disabled={isLoading}
        className="w-full px-4 py-3 bg-navy-500 text-white rounded-lg font-medium hover:bg-navy-600 focus:outline-none focus:ring-2 focus:ring-navy-500 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
    >
        {isLoading ? '처리 중...' : '저장'}
    </button>
);

const useUpbitKeyForm = () => {
    const router = useRouter();
    const [accessKey, setAccessKey] = useState('');
    const [secretKey, setSecretKey] = useState('');
    const [error, setError] = useState('');
    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({
        accessKey: '',
        secretKey: '',
    });
    const [isLoading, setIsLoading] = useState(false);

    const validateForm = () => {
        const newErrors = {
            accessKey: !accessKey.trim() ? 'Access Key는 필수값입니다.' : '',
            secretKey: !secretKey.trim() ? 'Secret Key는 필수값입니다.' : '',
        };
        setValidationErrors(newErrors);
        return !Object.values(newErrors).some((error) => error);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;

        try {
            setIsLoading(true);
            await upbitApi.registerUpbitApiKeys({ accessKey, secretKey });
            router.push('/dashboard');
        } catch (error) {
            console.error('Error:', error);
            setError('API 키 등록에 실패했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    const clearValidationError = (field: keyof ValidationErrors) => {
        if (validationErrors[field]) {
            setValidationErrors((prev) => ({ ...prev, [field]: '' }));
        }
    };

    return {
        accessKey,
        secretKey,
        error,
        validationErrors,
        isLoading,
        setAccessKey: (value: string) => {
            setAccessKey(value);
            clearValidationError('accessKey');
        },
        setSecretKey: (value: string) => {
            setSecretKey(value);
            clearValidationError('secretKey');
        },
        handleSubmit,
    };
};

export default function UpbitApiKeyPage() {
    const {
        accessKey,
        secretKey,
        error,
        validationErrors,
        isLoading,
        setAccessKey,
        setSecretKey,
        handleSubmit,
    } = useUpbitKeyForm();

    return (
        <div className="min-h-screen bg-white dark:bg-navy-900">
            <MainHeader title="everbit" showControls={false} />;
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
                <div className="bg-white dark:bg-navy-800 shadow rounded-lg p-4 sm:p-6">
                    <div className="flex flex-col lg:flex-row lg:justify-between lg:space-x-8">
                        <UpbitApiKeyDescription />
                        <UpbitApiKeyLink />
                    </div>

                    <form onSubmit={handleSubmit} className="mt-8 space-y-6">
                        <div className="space-y-4">
                            <FormInput
                                id="accessKey"
                                type="text"
                                value={accessKey}
                                onChange={setAccessKey}
                                error={validationErrors.accessKey}
                                label="Access Key"
                                placeholder="Access Key를 입력하세요"
                            />
                            <FormInput
                                id="secretKey"
                                type="password"
                                value={secretKey}
                                onChange={setSecretKey}
                                error={validationErrors.secretKey}
                                label="Secret Key"
                                placeholder="Secret Key를 입력하세요"
                            />
                            {error && <ErrorAlert message={error} />}
                        </div>
                        <SubmitButton isLoading={isLoading} />
                    </form>
                </div>
                <UpbitKeyGuide />
            </div>
        </div>
    );
}
