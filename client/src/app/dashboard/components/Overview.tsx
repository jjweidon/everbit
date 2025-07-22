import { useState } from 'react';
import { formatNumber, formatPercent } from '../utils/format';
import { OverviewData } from '../types';
import { MOCK_DATA } from '../constants';
import BotStatusIndicator from './BotStatusIndicator';

export default function Overview() {
    const [overviewData] = useState<OverviewData>(MOCK_DATA.overview);

    return (
        <div className="flex flex-col sm:gap-6">
            <div className="flex gap-4 sm:gap-6">
                {/* 좌측 Overview Cards */}
                <div className="flex-3 flex flex-col gap-2">
                    <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-3 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                        <h3 className="text-xs sm:text-base font-medium text-navy-600 dark:text-navy-300 mb-1 sm:mb-2 truncate">
                            총 자산
                        </h3>
                        <p className="text-[10px] xs:text-sm sm:text-xl lg:text-2xl xl:text-3xl font-bold text-navy-900 dark:text-white whitespace-nowrap">
                            {formatNumber(overviewData.totalAsset)}
                            <span className="text-[8px] xs:text-xs sm:text-sm ml-1">KRW</span>
                        </p>
                    </div>
                    <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-3 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                        <h3 className="text-xs sm:text-base font-medium text-navy-600 dark:text-navy-300 mb-1 sm:mb-2 truncate">
                            수익률
                        </h3>
                        <p
                            className={`text-[10px] xs:text-sm sm:text-xl lg:text-2xl xl:text-3xl font-bold ${overviewData.profitRate > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'} whitespace-nowrap`}
                        >
                            {formatPercent(overviewData.profitRate)}
                        </p>
                    </div>
                    <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-3 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                        <h3 className="text-xs sm:text-base font-medium text-navy-600 dark:text-navy-300 mb-1 sm:mb-2 truncate">
                            실현 손익
                        </h3>
                        <p
                            className={`text-[10px] xs:text-sm sm:text-xl lg:text-2xl xl:text-3xl font-bold ${overviewData.realizedProfit > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'} whitespace-nowrap`}
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
                            <h3 className="text-base sm:text-lg font-medium text-navy-900 dark:text-white truncate">
                                Bot 상태
                            </h3>
                            <BotStatusIndicator />
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
