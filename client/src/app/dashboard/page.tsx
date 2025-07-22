'use client';

import { useState } from 'react';
import { Overview, Portfolio, History, Settings, Navigation } from './components';
import { DashboardTab } from './types';
import MainHeader from '@/components/MainHeader';

export default function Dashboard() {
    const [selectedTab, setSelectedTab] = useState<DashboardTab>('overview');

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
            <Navigation selectedTab={selectedTab} setSelectedTab={setSelectedTab} />
            <div className="max-w-7xl mx-auto px-4 sm:px-16 lg:px-24 py-8">{renderContent()}</div>
        </div>
    );
}
