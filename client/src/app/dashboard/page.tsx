'use client';

import { useState, useEffect } from 'react';
import { Overview, Portfolio, History, Settings, Navigation } from './components';
import { DashboardTab } from './types';
import { MOCK_DATA } from './constants';
import MainHeader from '@/components/MainHeader';

export default function Dashboard() {
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [botStatus, setBotStatus] = useState(false);
    const [selectedTab, setSelectedTab] = useState<DashboardTab>('overview');

    useEffect(() => {
        const initializeDashboard = async () => {
            try {
                // 실제 데이터 로딩 로직 구현 필요
                setIsLoading(false);
            } catch (err) {
                setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
                setIsLoading(false);
            }
        };

        initializeDashboard();
    }, []);

    if (isLoading || error) {
        return (
            <div className="min-h-screen bg-gradient-to-b from-navy-500 to-navy-700">
                {error && (
                    <div className="flex items-center justify-center h-screen">
                        <div className="text-white bg-red-600 px-6 py-4 rounded-lg">
                            <p>{error}</p>
                        </div>
                    </div>
                )}
                {isLoading && (
                    <div className="flex items-center justify-center h-screen">
                        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-white"></div>
                    </div>
                )}
            </div>
        );
    }

    const renderContent = () => {
        switch (selectedTab) {
            case 'overview':
                return (
                    <Overview
                        overviewData={MOCK_DATA.overview}
                        botStatus={botStatus}
                        setBotStatus={setBotStatus}
                    />
                );
            case 'portfolio':
                return <Portfolio portfolioData={MOCK_DATA.portfolio} />;
            case 'history':
                return <History tradeHistoryData={MOCK_DATA.tradeHistory} />;
            case 'settings':
                return (
                    <Settings
                        botSettingsData={MOCK_DATA.botSettings}
                        backtestData={MOCK_DATA.backtest}
                    />
                );
            default:
                return null;
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-navy-50 to-white dark:from-navy-900 dark:to-navy-800">
            <MainHeader title="Dashboard" botStatus={botStatus} />
            <Navigation selectedTab={selectedTab} setSelectedTab={setSelectedTab} />
            <div className="max-w-7xl mx-auto px-4 sm:px-16 lg:px-24 py-8">{renderContent()}</div>
        </div>
    );
}
