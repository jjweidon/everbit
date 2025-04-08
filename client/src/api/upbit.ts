import { UpbitAccount } from '@/types/upbit';
import { apiClient } from './config';

export const upbitApi = {
    getAccounts: async (): Promise<UpbitAccount[]> => {
        console.log('업비트 계좌 정보 요청 시작', process.env.NEXT_PUBLIC_API_HOST);
        const response = await apiClient.get<UpbitAccount[]>('/upbit/accounts');
        console.log('업비트 계좌 정보 응답:', response.data);
        return response.data;
    }
}; 