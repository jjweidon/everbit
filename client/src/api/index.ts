import axios from 'axios';

// API 클라이언트 생성 (Next.js API Route를 통해 요청)
const apiClient = axios.create({
  baseURL: '/api/proxy',
  headers: {
    'Content-Type': 'application/json',
  },
});

// API 요청 함수들
export const api = {
  // 예시: 시세 데이터 가져오기
  async getMarketData(symbol: string) {
    const response = await apiClient.get(`?path=market-data/${symbol}`);
    return response.data;
  },
  
  // 예시: 트레이딩 신호 가져오기
  async getTradingSignals() {
    const response = await apiClient.get('?path=trading-signals');
    return response.data;
  },
  
  // 예시: 트레이딩 설정 저장
  async saveTradingSettings(settings: any) {
    const response = await apiClient.post('?path=settings', settings);
    return response.data;
  }
};

export default api; 