interface SettingsProps {
  botSettingsData: {
    algorithms: Array<{
      id: string;
      name: string;
      description: string;
    }>;
    currentSettings: {
      algorithm: string;
      params: {
        period: number;
        tradeRatio: number;
        stopLoss: number;
        takeProfit: number;
      };
    };
  };
  backtestData: {
    results: {
      cagr: number;
      mdd: number;
      winRate: number;
      profitFactor: number;
    };
  };
}

export default function Settings({ botSettingsData, backtestData }: SettingsProps) {
  return (
    <div className="space-y-6">
      <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-6 rounded-2xl shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
        <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">Bot 설정</h3>
        
        {/* Algorithm Selection */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">알고리즘 선택</label>
          <select
            className="w-full px-3 py-2 bg-white dark:bg-navy-800 border border-navy-300 dark:border-navy-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-navy-500 dark:focus:ring-navy-400 text-navy-900 dark:text-white transition-shadow duration-200"
            value={botSettingsData.currentSettings.algorithm}
          >
            {botSettingsData.algorithms.map(algo => (
              <option key={algo.id} value={algo.id}>{algo.name}</option>
            ))}
          </select>
        </div>

        {/* Parameters */}
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">기간 (일)</label>
            <input
              type="number"
              className="w-full px-3 py-2 bg-white dark:bg-navy-800 border border-navy-300 dark:border-navy-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-navy-500 dark:focus:ring-navy-400 text-navy-900 dark:text-white transition-shadow duration-200"
              value={botSettingsData.currentSettings.params.period}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">매매 비율 (%)</label>
            <input
              type="number"
              className="w-full px-3 py-2 bg-white dark:bg-navy-800 border border-navy-300 dark:border-navy-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-navy-500 dark:focus:ring-navy-400 text-navy-900 dark:text-white transition-shadow duration-200"
              value={botSettingsData.currentSettings.params.tradeRatio}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">손절 비율 (%)</label>
            <input
              type="number"
              className="w-full px-3 py-2 bg-white dark:bg-navy-800 border border-navy-300 dark:border-navy-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-navy-500 dark:focus:ring-navy-400 text-navy-900 dark:text-white transition-shadow duration-200"
              value={botSettingsData.currentSettings.params.stopLoss}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">익절 비율 (%)</label>
            <input
              type="number"
              className="w-full px-3 py-2 bg-white dark:bg-navy-800 border border-navy-300 dark:border-navy-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-navy-500 dark:focus:ring-navy-400 text-navy-900 dark:text-white transition-shadow duration-200"
              value={botSettingsData.currentSettings.params.takeProfit}
            />
          </div>
        </div>

        <button className="mt-6 w-full px-4 py-2 bg-navy-500 hover:bg-navy-600 text-white rounded-lg transition-all duration-200 shadow-lg shadow-navy-500/30">
          설정 저장
        </button>
      </div>

      {/* Backtesting Results */}
      <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-6 rounded-2xl shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
        <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">백테스팅 결과</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="p-4 bg-white/50 dark:bg-navy-700/50 rounded-xl backdrop-blur-sm">
            <p className="text-sm text-navy-600 dark:text-navy-300">CAGR</p>
            <p className="text-xl font-bold text-navy-900 dark:text-white">{backtestData.results.cagr}%</p>
          </div>
          <div className="p-4 bg-white/50 dark:bg-navy-700/50 rounded-xl backdrop-blur-sm">
            <p className="text-sm text-navy-600 dark:text-navy-300">MDD</p>
            <p className="text-xl font-bold text-navy-900 dark:text-white">{backtestData.results.mdd}%</p>
          </div>
          <div className="p-4 bg-white/50 dark:bg-navy-700/50 rounded-xl backdrop-blur-sm">
            <p className="text-sm text-navy-600 dark:text-navy-300">승률</p>
            <p className="text-xl font-bold text-navy-900 dark:text-white">{backtestData.results.winRate}%</p>
          </div>
          <div className="p-4 bg-white/50 dark:bg-navy-700/50 rounded-xl backdrop-blur-sm">
            <p className="text-sm text-navy-600 dark:text-navy-300">수익 팩터</p>
            <p className="text-xl font-bold text-navy-900 dark:text-white">{backtestData.results.profitFactor}</p>
          </div>
        </div>
      </div>
    </div>
  );
} 