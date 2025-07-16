import { formatNumber, formatPercent } from '../utils/format';

interface PortfolioProps {
  portfolioData: {
    coins: Array<{
      symbol: string;
      entryPrice: number;
      currentPrice: number;
      profitRate: number;
      amount: number;
    }>;
  };
}

export default function Portfolio({ portfolioData }: PortfolioProps) {
  return (
    <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-6 rounded-2xl shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-navy-200 dark:divide-navy-700">
          <thead>
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">코인</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">보유수량</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">진입가</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">현재가</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">수익률</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-navy-200 dark:divide-navy-700">
            {portfolioData.coins.map((coin, index) => (
              <tr key={index} className="hover:bg-navy-50 dark:hover:bg-navy-700/50 transition-colors duration-150">
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-navy-900 dark:text-white">{coin.symbol}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">{coin.amount}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">{formatNumber(coin.entryPrice)}원</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">{formatNumber(coin.currentPrice)}원</td>
                <td className={`px-6 py-4 whitespace-nowrap text-sm font-medium ${coin.profitRate > 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}`}>
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