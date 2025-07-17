import { formatNumber, formatPercent } from '../utils/format';
import { PortfolioData } from '../types';

interface PortfolioProps {
    portfolioData: Readonly<PortfolioData>;
}

export default function Portfolio({ portfolioData }: PortfolioProps) {
    return (
        <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
            {/* 모바일 뷰 */}
            <div className="grid grid-cols-1 gap-4 sm:hidden">
                {portfolioData.coins.map((coin, index) => (
                    <div key={index} className="bg-navy-50/50 dark:bg-navy-800 rounded-xl p-4">
                        <div className="flex justify-between items-center mb-3">
                            <span className="text-lg font-medium text-navy-900 dark:text-white font-kimm">
                                {coin.symbol}
                            </span>
                            <span
                                className={`text-sm font-medium ${coin.profitRate > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}`}
                            >
                                {formatPercent(coin.profitRate)}
                            </span>
                        </div>
                        <div className="grid grid-cols-2 gap-2 text-sm">
                            <div>
                                <p className="text-navy-500 dark:text-navy-400">보유수량</p>
                                <p className="text-navy-600 dark:text-navy-300">{coin.amount}</p>
                            </div>
                            <div>
                                <p className="text-navy-500 dark:text-navy-400">진입가</p>
                                <p className="text-navy-600 dark:text-navy-300">
                                    {formatNumber(coin.entryPrice)}원
                                </p>
                            </div>
                            <div className="col-span-2">
                                <p className="text-navy-500 dark:text-navy-400">현재가</p>
                                <p className="text-navy-600 dark:text-navy-300">
                                    {formatNumber(coin.currentPrice)}원
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
                                코인
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                보유수량
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                진입가
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                현재가
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                수익률
                            </th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-navy-200 dark:divide-navy-700">
                        {portfolioData.coins.map((coin, index) => (
                            <tr
                                key={index}
                                className="hover:bg-navy-50 dark:hover:bg-navy-700/50 transition-colors duration-150"
                            >
                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-navy-900 dark:text-white font-kimm">
                                    {coin.symbol}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                    {coin.amount}
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                    {formatNumber(coin.entryPrice)}원
                                </td>
                                <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                    {formatNumber(coin.currentPrice)}원
                                </td>
                                <td
                                    className={`px-6 py-4 whitespace-nowrap text-sm font-medium ${coin.profitRate > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}`}
                                >
                                    {formatPercent(coin.profitRate)}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
