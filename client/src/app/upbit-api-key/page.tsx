'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import UpbitApiKeyForm from '../../components/UpbitApiKeyForm';
import { Box, Typography, Button } from '@mui/material';

export default function UpbitApiKeyPage() {
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
                throw new Error('API 키 등록에 실패했습니다.');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('API 키 등록에 실패했습니다.');
        }
    };

    return (
        <Box sx={{ p: 3 }}>
            <Typography variant="h4" gutterBottom>
                업비트 API 키 등록
            </Typography>
            <Typography variant="body1" paragraph>
                업비트 API를 사용하기 위해 API 키를 등록해야 합니다.
                <Button
                    variant="text"
                    color="primary"
                    onClick={() => window.open('https://upbit.com/mypage/open_api_management', '_blank')}
                >
                    업비트 API 키 발급 페이지
                </Button>
                에서 API 키를 발급받은 후, 아래 폼에 입력해주세요.
            </Typography>
            <UpbitApiKeyForm onSubmit={handleSubmit} />
        </Box>
    );
} 