import { FaFileExport } from 'react-icons/fa';
import { formatNumber } from '../utils/format';
import { TradeHistoryData } from '../types';

interface HistoryProps {
    tradeHistoryData: Readonly<TradeHistoryData>;
}

export default function History({ tradeHistoryData }: HistoryProps) {
    return (
        <div className="space-y-6">
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <div className="flex flex-row justify-between items-center mb-6">
                    <h3 className="text-lg font-medium text-navy-900 dark:text-white">거래 내역</h3>
                    <button className="flex items-center justify-center space-x-2 px-4 py-2 bg-navy-500 hover:bg-navy-600 text-white rounded-md transition-colors duration-200 shadow-lg shadow-navy-500/30">
                        <FaFileExport />
                        <span>CSV 내보내기</span>
                    </button>
                </div>

                {/* 모바일 뷰 */}
                <div className="grid grid-cols-1 gap-4 sm:hidden">
                    {tradeHistoryData.trades.map((trade, index) => (
                        <div key={index} className="bg-navy-50/50 dark:bg-navy-800 rounded-md p-4">
                            <div className="flex justify-between items-start mb-3">
                                <div>
                                    <span className="text-base font-medium text-navy-900 dark:text-white font-kimm block">
                                        {trade.symbol}
                                    </span>
                                    <span className="text-xs text-navy-500 dark:text-navy-400 block mt-1">
                                        {trade.time}
                                    </span>
                                </div>
                                <span
                                    className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${
                                        trade.type === 'entry'
                                            ? 'trade-badge-entry'
                                            : 'trade-badge-exit'
                                    }`}
                                >
                                    {trade.type === 'entry' ? '매수' : '매도'}
                                </span>
                            </div>
                            <div className="grid grid-cols-2 gap-2 text-sm">
                                <div>
                                    <p className="text-navy-500 dark:text-navy-400">수량</p>
                                    <p className="text-navy-600 dark:text-navy-300">
                                        {trade.amount}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-navy-500 dark:text-navy-400">수익</p>
                                    <p className="text-navy-600 dark:text-navy-300">
                                        {trade.profit ? `${formatNumber(trade.profit)}원` : '-'}
                                    </p>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                {/* 데스크톱 뷰 */}
                <div className="hidden sm:block overflow-x-auto">
                    <table className="min-w-full divide-y divide-navy-200 dark:divide-navy-700">
                        <thead>
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                    시간
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                    코인
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                    유형
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                    수량
                                </th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                    수익
                                </th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-navy-200 dark:divide-navy-700">
                            {tradeHistoryData.trades.map((trade, index) => (
                                <tr
                                    key={index}
                                    className="hover:bg-navy-50 dark:hover:bg-navy-700/50 transition-colors duration-150"
                                >
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                        {trade.time}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-navy-900 dark:text-white font-kimm">
                                        {trade.symbol}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                        <span
                                            className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                                trade.type === 'entry'
                                                    ? 'trade-badge-entry'
                                                    : 'trade-badge-exit'
                                            }`}
                                        >
                                            {trade.type === 'entry' ? '매수' : '매도'}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                        {trade.amount}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                        {trade.profit ? `${formatNumber(trade.profit)}원` : '-'}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
