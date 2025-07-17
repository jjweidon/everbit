import { formatNumber, formatPercent } from '../utils/format';
import { OverviewData } from '../types';

interface OverviewProps {
  overviewData: Readonly<OverviewData>;
  botStatus: boolean;
  setBotStatus: (status: boolean) => void;
}

export default function Overview({ overviewData, botStatus, setBotStatus }: OverviewProps) {
  return (
    <div className="space-y-6">
      {/* Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-6 rounded-2xl shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
          <h3 className="text-base font-medium text-navy-600 dark:text-navy-300 mb-2">총 자산</h3>
          <p className="text-3xl font-bold text-navy-900 dark:text-white font-kimm">{formatNumber(overviewData.totalAsset)}<span className="text-sm">KRW</span></p>
        </div>
        <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-6 rounded-2xl shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
          <h3 className="text-base font-medium text-navy-600 dark:text-navy-300 mb-2">수익률</h3>
          <p className={`text-3xl font-bold ${overviewData.profitRate > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'} font-kimm`}>
            {formatPercent(overviewData.profitRate)}
          </p>
        </div>
        <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-6 rounded-2xl shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
          <h3 className="text-base font-medium text-navy-600 dark:text-navy-300 mb-2">실현 손익</h3>
          <p className={`text-3xl font-bold ${overviewData.realizedProfit > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'} font-kimm`}>
            {formatNumber(overviewData.realizedProfit)}<span className="text-sm">KRW</span>
          </p>
        </div>
      </div>

      {/* Bot Status */}
      <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-6 rounded-2xl shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-medium text-navy-900 dark:text-white">Bot 상태</h3>
          <div className="flex items-center space-x-4">
            <span className={`inline-block w-3 h-3 rounded-full ${botStatus ? 'bg-emerald-500 animate-pulse' : 'bg-red-500'}`}></span>
            <span className="text-navy-600 dark:text-navy-300">{botStatus ? '실행 중' : '중지됨'}</span>
            <button
              onClick={() => setBotStatus(!botStatus)}
              className={`px-4 py-2 rounded-lg text-white transition-all duration-200 ${
                botStatus 
                  ? 'bg-red-500 hover:bg-red-600 shadow-lg shadow-red-500/30' 
                  : 'bg-emerald-500 hover:bg-emerald-600 shadow-lg shadow-emerald-500/30'
              }`}
            >
              {botStatus ? '중지' : '시작'}
            </button>
          </div>
        </div>
      </div>

      {/* Notifications */}
      <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-6 rounded-2xl shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
        <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">게시판</h3>
        <div className="space-y-3">
          {overviewData.notifications.map((notification, index) => (
            <div
              key={index}
              className={`p-4 rounded-xl backdrop-blur-xl ${
                notification.type === 'error' 
                  ? 'bg-red-50/50 text-red-700 dark:bg-red-900/20 dark:text-red-400' 
                  : 'bg-navy-50/50 text-navy-700 dark:bg-navy-900/20 dark:text-navy-300'
              }`}
            >
              {notification.message}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
} 