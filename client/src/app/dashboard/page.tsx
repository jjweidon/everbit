'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Overview from './components/Overview';
import Portfolio from './components/Portfolio';
import History from './components/History';
import Settings from './components/Settings';
import Navigation from './components/Navigation';

export default function Dashboard() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [botStatus, setBotStatus] = useState(false);
  const [selectedTab, setSelectedTab] = useState('overview');

  // 임시 데이터 (실제로는 API에서 가져와야 함)
  const overviewData = {
    totalAsset: 15000000,
    profitRate: 12.5,
    realizedProfit: 1800000,
    notifications: [
      { type: 'error', message: '업비트 API 키가 만료되었습니다.' },
      { type: 'info', message: '새로운 버전의 봇이 출시되었습니다.' }
    ]
  };

  const portfolioData = {
    coins: [
      { symbol: 'BTC', entryPrice: 58000000, currentPrice: 62000000, profitRate: 6.89, amount: 0.1 },
      { symbol: 'ETH', entryPrice: 3500000, currentPrice: 3200000, profitRate: -8.57, amount: 1.5 }
    ]
  };

  const tradeHistoryData = {
    trades: [
      { time: '2024-03-15 14:30', symbol: 'BTC', type: 'entry', amount: 0.1, profit: null },
      { time: '2024-03-15 12:15', symbol: 'ETH', type: 'exit', amount: 0.5, profit: 150000 }
    ]
  };

  const botSettingsData = {
    algorithms: [
      { id: 'momentum', name: '모멘텀 전략', description: '가격 모멘텀을 기반으로 한 매매 전략' },
      { id: 'ema', name: 'EMA 크로스', description: '이동평균선 크로스를 활용한 매매 전략' }
    ],
    currentSettings: {
      algorithm: 'momentum',
      params: {
        period: 14,
        tradeRatio: 30,
        stopLoss: 5,
        takeProfit: 10
      }
    }
  };

  const backtestData = {
    results: {
      cagr: 45.2,
      mdd: 28.5,
      winRate: 62.8,
      profitFactor: 1.85
    }
  };

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
        return <Overview overviewData={overviewData} botStatus={botStatus} setBotStatus={setBotStatus} />;
      case 'portfolio':
        return <Portfolio portfolioData={portfolioData} />;
      case 'history':
        return <History tradeHistoryData={tradeHistoryData} />;
      case 'settings':
        return <Settings botSettingsData={botSettingsData} backtestData={backtestData} />;
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-navy-50 to-white dark:from-navy-900 dark:to-navy-800">
      {/* Header */}
      <div className="bg-gradient-to-r from-navy-600 to-navy-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex justify-between items-center">
            <h1 className="text-2xl font-bold text-white font-kimm">Dashboard</h1>
            <div className="flex items-center space-x-4 bg-white/10 px-4 py-2 rounded-full">
              <span className={`inline-block w-3 h-3 rounded-full ${botStatus ? 'bg-emerald-500 animate-pulse' : 'bg-red-500'}`}></span>
              <span className="text-white/90 text-sm font-medium">{botStatus ? '실행 중' : '중지됨'}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <Navigation selectedTab={selectedTab} setSelectedTab={setSelectedTab} />

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {renderContent()}
      </div>
    </div>
  );
} 