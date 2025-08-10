'use client';

import { useState, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import { Overview, Portfolio, History, Settings, Navigation } from './components';
import { DashboardTab } from './types';
import MainHeader from '@/components/MainHeader';

export default function Dashboard() {
    const searchParams = useSearchParams();
    const [selectedTab, setSelectedTab] = useState<DashboardTab>('overview');

    // URL 쿼리 파라미터에서 초기 탭 상태를 가져옵니다
    useEffect(() => {
        const tabParam = searchParams.get('tab') as DashboardTab;
        if (tabParam && ['overview', 'portfolio', 'history', 'settings'].includes(tabParam)) {
            setSelectedTab(tabParam);
        }
    }, [searchParams]);

    // 탭 변경 시 URL 쿼리 파라미터를 업데이트합니다
    const handleTabChange = (tab: DashboardTab) => {
        setSelectedTab(tab);
        // URL만 업데이트하고 페이지 새로고침은 방지합니다
        const params = new URLSearchParams(searchParams.toString());
        params.set('tab', tab);
        const newUrl = `/dashboard?${params.toString()}`;
        window.history.pushState({}, '', newUrl);
    };

    const renderContent = () => {
        switch (selectedTab) {
            case 'overview':
                return <Overview />;
            case 'portfolio':
                return <Portfolio />;
            case 'history':
                return <History />;
            case 'settings':
                return <Settings />;
            default:
                return null;
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-navy-50 to-white dark:from-darkBg dark:to-darkBg">
            <MainHeader title="everbit" />
            <Navigation selectedTab={selectedTab} setSelectedTab={handleTabChange} />
            <div className="max-w-7xl mx-auto px-4 sm:px-16 lg:px-24 py-8">{renderContent()}</div>
        </div>
    );
}
