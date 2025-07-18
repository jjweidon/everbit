'use client';

import { useEffect, useState } from 'react';
import { UserList, TradeLog, ServerLog } from './components';
import MainHeader from '@/components/MainHeader';

export default function AdminPage() {
  const [activeTab, setActiveTab] = useState('users');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

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
    switch (activeTab) {
      case 'users':
        return <UserList />;
      case 'trade-logs':
        return <TradeLog />;
      case 'server-logs':
        return <ServerLog />;
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-navy-50 to-white dark:from-darkBg dark:to-darkBg">
      <MainHeader title="everbit" showControls={false} />
      <div className="sticky top-0 z-20 bg-white dark:bg-navy-900 backdrop-blur-xl border-b border-navy-200 dark:border-navy-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <nav className="flex space-x-8">
            <button
              onClick={() => setActiveTab('users')}
              className={`
                relative flex items-center px-4 py-4 text-sm font-medium 
                transition-all duration-200
                ${
                  activeTab === 'users'
                    ? 'text-navy-600 dark:text-navy-300'
                    : 'text-navy-500 hover:text-navy-700 dark:text-navy-400 dark:hover:text-white'
                }
              `}
            >
              <span>사용자 관리</span>
              {activeTab === 'users' && (
                <div className="absolute bottom-0 left-0 w-full h-0.5 bg-navy-500" />
              )}
            </button>
            <button
              onClick={() => setActiveTab('trade-logs')}
              className={`
                relative flex items-center px-4 py-4 text-sm font-medium 
                transition-all duration-200
                ${
                  activeTab === 'trade-logs'
                    ? 'text-navy-600 dark:text-navy-300'
                    : 'text-navy-500 hover:text-navy-700 dark:text-navy-400 dark:hover:text-white'
                }
              `}
            >
              <span>매매 로그</span>
              {activeTab === 'trade-logs' && (
                <div className="absolute bottom-0 left-0 w-full h-0.5 bg-navy-500" />
              )}
            </button>
            <button
              onClick={() => setActiveTab('server-logs')}
              className={`
                relative flex items-center px-4 py-4 text-sm font-medium 
                transition-all duration-200
                ${
                  activeTab === 'server-logs'
                    ? 'text-navy-600 dark:text-navy-300'
                    : 'text-navy-500 hover:text-navy-700 dark:text-navy-400 dark:hover:text-white'
                }
              `}
            >
              <span>서버 로그</span>
              {activeTab === 'server-logs' && (
                <div className="absolute bottom-0 left-0 w-full h-0.5 bg-navy-500" />
              )}
            </button>
          </nav>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-16 lg:px-24 py-8">
        {renderContent()}
      </div>
    </div>
  );
}