'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import UpbitApiKeyForm from '../../components/UpbitApiKeyForm';
import { Box, Text, Button, Alert, AlertIcon, AlertTitle, AlertDescription } from '@chakra-ui/react';

export default function UpbitApiKeyPage() {
    const router = useRouter();
    const [error, setError] = React.useState<string | null>(null);

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
        <Box p={6}>
            <Text fontSize="2xl" fontWeight="bold" mb={4}>
                업비트 API 키 등록
            </Text>
            {error && (
                <Alert status="error" mb={4}>
                    <AlertIcon />
                    <AlertTitle>오류!</AlertTitle>
                    <AlertDescription>{error}</AlertDescription>
                </Alert>
            )}
            <Text mb={6}>
                업비트 API를 사용하기 위해 API 키를 등록해야 합니다.
                <Button
                    variant="link"
                    colorScheme="blue"
                    onClick={() => window.open('https://upbit.com/mypage/open_api_management', '_blank')}
                >
                    업비트 API 키 발급 페이지
                </Button>
                에서 API 키를 발급받은 후, 아래 폼에 입력해주세요.
            </Text>
            <UpbitApiKeyForm onSubmit={handleSubmit} />
        </Box>
    );
} 