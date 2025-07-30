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
                id: 'momentum',
                name: '모멘텀 전략',
                description: '가격 모멘텀을 기반으로 한 매매 전략',
            },
            { id: 'ema', name: 'EMA 크로스', description: '이동평균선 크로스를 활용한 매매 전략' },
            { id: 'rsi', name: 'RSI 전략', description: 'RSI 지표를 활용한 매매 전략' },
            { id: 'bollinger', name: '볼린저 밴드', description: '볼린저 밴드를 활용한 매매 전략' },
        ],
        currentSettings: {
            botSettingId: '1',
            strategy: 'momentum',
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

export const STRATEGIES = [
    { value: 'STOCH_RSI', label: 'Stoch RSI', description: 'Stoch RSI를 이용한 전략' },
    { value: 'RSI_BB', label: 'RSI + 볼린저밴드', description: 'RSI와 볼린저밴드를 결합한 전략' },
    { value: 'EMA_MOMENTUM', label: 'EMA 크로스 + 모멘텀', description: 'EMA(9) vs EMA(21) 크로스와 ADX 모멘텀 필터를 결합한 전략' },
    { value: 'BB_MOMENTUM', label: '볼린저밴드 + 모멘텀', description: '볼린저밴드 평균회귀와 모멘텀 필터를 결합한 전략' },
    { value: 'GOLDEN_CROSS', label: '골든크로스', description: '50일 EMA와 200일 EMA의 크로스를 이용한 장기 추세추종 전략' },
    { value: 'ENSEMBLE', label: '앙상블 전략', description: '여러 전략의 시그널을 종합적으로 분석하여 매매하는 전략' },
];

export const MARKETS = [
    'BTC', 'ETH', 'SOL', 'DOGE', 'USDT', 'STRIKE', 'XRP', 'PENGU', 'ARDR', 'STRAX', 'ENS', 'AERGO'
];
