import React, { useState } from 'react';
import { Box, TextField, Button, Typography, Paper } from '@mui/material';

interface UpbitApiKeyFormProps {
    onSubmit: (accessKey: string, secretKey: string) => void;
}

const UpbitApiKeyForm: React.FC<UpbitApiKeyFormProps> = ({ onSubmit }) => {
    const [accessKey, setAccessKey] = useState('');
    const [secretKey, setSecretKey] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSubmit(accessKey, secretKey);
    };

    return (
        <Paper elevation={3} sx={{ p: 3, maxWidth: 500, mx: 'auto', mt: 4 }}>
            <Typography variant="h5" gutterBottom>
                업비트 API 키 등록
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
                업비트에서 발급받은 API 키를 입력해주세요.
            </Typography>
            <Box component="form" onSubmit={handleSubmit} sx={{ mt: 2 }}>
                <TextField
                    fullWidth
                    label="Access Key"
                    value={accessKey}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setAccessKey(e.target.value)}
                    margin="normal"
                    required
                />
                <TextField
                    fullWidth
                    label="Secret Key"
                    value={secretKey}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSecretKey(e.target.value)}
                    margin="normal"
                    required
                    type="password"
                />
                <Button
                    type="submit"
                    variant="contained"
                    fullWidth
                    sx={{ mt: 2 }}
                >
                    API 키 등록
                </Button>
            </Box>
        </Paper>
    );
};

export default UpbitApiKeyForm; 