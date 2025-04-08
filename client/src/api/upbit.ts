import { UpbitAccount } from '@/types/upbit';
import { apiClient } from './config';

export const upbitApi = {
    getAccounts: async (): Promise<UpbitAccount[]> => {
        const response = await apiClient.get<UpbitAccount[]>('/upbit/accounts');
        console.log('업비트 계좌 정보 응답:', response.data);
        return response.data;
    }
}; 