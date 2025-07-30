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
