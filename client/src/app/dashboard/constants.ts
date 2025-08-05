import { IconType } from 'react-icons';
import { FaChartLine, FaChartBar, FaHistory, FaRobot } from 'react-icons/fa';
import { DashboardTab } from './types';

export const DASHBOARD_TABS: { id: DashboardTab; label: string; icon: IconType }[] = [
    { id: 'overview', label: '개요', icon: FaChartLine },
    { id: 'portfolio', label: '포트폴리오', icon: FaChartBar },
    { id: 'history', label: '거래 내역', icon: FaHistory },
    { id: 'settings', label: '봇 설정', icon: FaRobot },
];

// 임시 데이터 (추후 API 연동 시 제거)
export const MOCK_DATA = {
    overview: {
        totalAsset: 15000000,
        profitRate: 12.5,
        realizedProfit: 1800000,
        notifications: [
            { type: 'error' as const, message: '업비트 API 키가 만료되었습니다.' },
            { type: 'info' as const, message: '새로운 버전의 봇이 출시되었습니다.' },
        ],
    },
    portfolio: {
        coins: [
            {
                symbol: 'BTC',
                entryPrice: 58000000,
                currentPrice: 62000000,
                profitRate: 6.89,
                amount: 0.1,
            },
            {
                symbol: 'ETH',
                entryPrice: 3500000,
                currentPrice: 3200000,
                profitRate: -8.57,
                amount: 1.5,
            },
        ],
    },
    tradeHistory: {
        trades: [
            {
                time: '2024-03-15 14:30',
                symbol: 'BTC',
                type: 'entry' as const,
                amount: 0.1,
                profit: null,
            },
            {
                time: '2024-03-15 12:15',
                symbol: 'ETH',
                type: 'exit' as const,
                amount: 0.5,
                profit: 150000,
            },
        ],
    },
    botSettings: {
        algorithms: [
            {
                id: 'BOLLINGER_MEAN_REVERSION',
                name: '볼린저 평균회귀',
                description: '가격이 볼린저 밴드 하단을 터치하고 과매도(RSI) 상태일 때, 반등을 노리는 평균 회귀 전략',
            },
            {
                id: 'BB_MOMENTUM',
                name: '볼린저 + 모멘텀',
                description: '가격이 볼린저 밴드 수렴 구간에서 이탈할 때, 모멘텀 지표로 추세 방향을 판단하여 진입하는 전략',
            },
            {
                id: 'EMA_MOMENTUM',
                name: 'EMA 모멘텀',
                description: '단기/중기 이동평균(EMA 9/21)의 교차와 MACD로 추세를 판단해 추세에 진입하는 전략',
            },
            {
                id: 'ENSEMBLE',
                name: '앙상블',
                description: '여러 개별 전략의 매수/매도 시그널을 조합하여 신뢰도 높은 매매 타이밍을 포착하는 전략',
            },
            {
                id: 'ENHANCED_ENSEMBLE',
                name: '강화 앙상블',
                description: '볼린저 평균회귀 등 복수 전략을 통합 분석해 더 정교하게 매매 타이밍을 결정하는 전략',
            },
        ],
        currentSettings: {
            botSettingId: '1',
            buyStrategy: 'TRIPLE_INDICATOR_MODERATE',
            sellStrategy: 'TRIPLE_INDICATOR_MODERATE',
            marketList: ['KRW-BTC', 'KRW-ETH'],
            baseOrderAmount: 100000,
            maxOrderAmount: 1000000,
            startTime: '09:00',
            endTime: '18:00',
            candleInterval: '1m',
            candleCount: 100,
        },
    },
    backtest: {
        results: {
            cagr: 45.2,
            mdd: 28.5,
            winRate: 62.8,
            profitFactor: 1.85,
        },
    },
} as const;

// 봇 설정 관련 상수
export const BASE_ORDER_AMOUNT = 6000;
export const MAX_ORDER_AMOUNT = 100000;

export const CANDLE_INTERVALS = [
    { value: 'ONE', label: '1분' },
    { value: 'THREE', label: '3분' },
    { value: 'FIVE', label: '5분' },
    { value: 'TEN', label: '10분' },
    { value: 'FIFTEEN', label: '15분' },
    { value: 'THIRTY', label: '30분' },
    { value: 'SIXTY', label: '1시간' },
    { value: 'TWO_FORTY', label: '4시간' },
];

export const MARKETS = [
    'BTC', 'ETH', 'SOL', 'DOGE', 'USDT', 'STRIKE', 'XRP', 'PENGU', 'ARDR', 'STRAX', 'ENS', 'AERGO'
];
