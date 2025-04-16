import axios from 'axios';

export interface UpbitApiKeyResponse {
    success: boolean;
    error?: string;
}

export const updateUpbitApiKey = async (accessKey: string, secretKey: string): Promise<UpbitApiKeyResponse> => {
    try {
        const response = await axios.patch('/api/members/me/upbit-key', {
            accessKey,
            secretKey
        }, {
            headers: {
                'Authorization': localStorage.getItem('Authorization') || ''
            }
        });
        return { success: true };
    } catch (error) {
        return { 
            success: false, 
            error: error instanceof Error ? error.message : '업비트 API 키 등록에 실패했습니다.' 
        };
    }
}; 