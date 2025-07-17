import { formatNumber, formatPercent } from '../utils/format';
import { OverviewData } from '../types';

interface OverviewProps {
    overviewData: Readonly<OverviewData>;
    botStatus: boolean;
    setBotStatus: (status: boolean) => void;
}

export default function Overview({ overviewData, botStatus, setBotStatus }: OverviewProps) {
    return (
        <div className="flex flex-col sm:gap-6">
            <div className="flex gap-4 sm:gap-6">
                {/* 좌측 Overview Cards */}
                <div className="flex-3 flex flex-col gap-2">
                    <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-3 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                        <h3 className="text-xs sm:text-base font-medium text-navy-600 dark:text-navy-300 mb-1 sm:mb-2 truncate">
                            총 자산
                        </h3>
                        <p className="text-[10px] xs:text-sm sm:text-xl lg:text-2xl xl:text-3xl font-bold text-navy-900 dark:text-white font-kimm whitespace-nowrap">
                            {formatNumber(overviewData.totalAsset)}
                            <span className="text-[8px] xs:text-xs sm:text-sm ml-1">KRW</span>
                        </p>
                    </div>
                    <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-3 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                        <h3 className="text-xs sm:text-base font-medium text-navy-600 dark:text-navy-300 mb-1 sm:mb-2 truncate">
                            수익률
                        </h3>
                        <p
                            className={`text-[10px] xs:text-sm sm:text-xl lg:text-2xl xl:text-3xl font-bold ${overviewData.profitRate > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'} font-kimm whitespace-nowrap`}
                        >
                            {formatPercent(overviewData.profitRate)}
                        </p>
                    </div>
                    <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-3 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                        <h3 className="text-xs sm:text-base font-medium text-navy-600 dark:text-navy-300 mb-1 sm:mb-2 truncate">
                            실현 손익
                        </h3>
                        <p
                            className={`text-[10px] xs:text-sm sm:text-xl lg:text-2xl xl:text-3xl font-bold ${overviewData.realizedProfit > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'} font-kimm whitespace-nowrap`}
                        >
                            {formatNumber(overviewData.realizedProfit)}
                            <span className="text-[8px] xs:text-xs sm:text-sm ml-1">KRW</span>
                        </p>
                    </div>
                </div>

                {/* 우측 Bot Status & 게시판 */}
                <div className="flex-1 flex flex-col gap-4">
                    {/* Bot Status */}
                    <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-3 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 sm:gap-0">
                            <div className="flex items-center gap-3">
                                <h3 className="text-base sm:text-lg font-medium text-navy-900 dark:text-white truncate">
                                    Bot 상태
                                </h3>
                                <div className="flex items-center gap-2 shrink-0">
                                    <span
                                        className={`inline-block w-2 h-2 sm:w-3 sm:h-3 rounded-full ${botStatus ? 'bg-emerald-500 animate-pulse' : 'bg-red-500'}`}
                                    ></span>
                                    <span className="text-sm text-navy-600 dark:text-navy-300">
                                        {botStatus ? '실행 중' : '중지됨'}
                                    </span>
                                </div>
                            </div>
                            <button
                                onClick={() => setBotStatus(!botStatus)}
                                className={`w-full sm:w-auto px-4 py-2 rounded-lg text-white text-sm transition-all duration-200 shrink-0 ${
                                    botStatus
                                        ? 'bg-red-500 hover:bg-red-600 shadow-lg shadow-red-500/30'
                                        : 'bg-emerald-500 hover:bg-emerald-600 shadow-lg shadow-emerald-500/30'
                                }`}
                            >
                                {botStatus ? '중지' : '시작'}
                            </button>
                        </div>
                    </div>

                    {/* 게시판 */}
                    <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-3 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50 flex-1">
                        <h3 className="text-base sm:text-lg font-medium text-navy-900 dark:text-white mb-3 sm:mb-4 truncate">
                            게시판
                        </h3>
                        <div className="space-y-2 sm:space-y-3">
                            {overviewData.notifications.map((notification, index) => (
                                <div
                                    key={index}
                                    className={`p-2 sm:p-4 rounded-lg sm:rounded-xl text-[10px] xs:text-xs sm:text-sm backdrop-blur-xl ${
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
            </div>
        </div>
    );
}
