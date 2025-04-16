'use client';

import * as React from 'react';
import { useState } from 'react';
import { Button, TextField, Box, Typography } from '@mui/material';

interface UpbitApiKeyFormProps {
    onSubmit: (accessKey: string, secretKey: string) => void;
}

export default function UpbitApiKeyForm({ onSubmit }: UpbitApiKeyFormProps) {
    const [accessKey, setAccessKey] = useState('');
    const [secretKey, setSecretKey] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSubmit(accessKey, secretKey);
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ mt: 3 }}>
            <Typography variant="h6" gutterBottom>
                Upbit API 키 설정
            </Typography>
            <TextField
                fullWidth
                label="Access Key"
                value={accessKey}
                onChange={(e) => setAccessKey(e.target.value)}
                margin="normal"
                required
            />
            <TextField
                fullWidth
                label="Secret Key"
                value={secretKey}
                onChange={(e) => setSecretKey(e.target.value)}
                margin="normal"
                required
                type="password"
            />
            <Button
                type="submit"
                variant="contained"
                color="primary"
                fullWidth
                sx={{ mt: 2 }}
            >
                저장
            </Button>
        </Box>
    );
} 