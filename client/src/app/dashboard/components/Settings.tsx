import { BotSettingsData, BacktestData } from '../types';
import { useState } from 'react';

interface SettingsProps {
    botSettingsData: Readonly<BotSettingsData>;
    backtestData: Readonly<BacktestData>;
}

export default function Settings({ botSettingsData, backtestData }: SettingsProps) {
    const [settings, setSettings] = useState(botSettingsData.currentSettings);

    const handleAlgorithmChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setSettings((prev) => ({
            ...prev,
            algorithm: e.target.value,
        }));
    };

    const handleParamChange =
        (param: keyof typeof settings.params) => (e: React.ChangeEvent<HTMLInputElement>) => {
            setSettings((prev) => ({
                ...prev,
                params: {
                    ...prev.params,
                    [param]: Number(e.target.value),
                },
            }));
        };

    const handleSave = () => {
        // TODO: API 연동 시 저장 로직 구현
        console.log('Settings saved:', settings);
    };

    return (
        <div className="space-y-4 sm:space-y-6">
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-base sm:text-lg font-medium text-navy-900 dark:text-white mb-4">
                    Bot 설정
                </h3>

                {/* Algorithm Selection */}
                <div className="mb-4 sm:mb-6">
                    <label className="block text-xs sm:text-sm font-medium text-navy-700 dark:text-navy-300 mb-1.5 sm:mb-2">
                        알고리즘 선택
                    </label>
                    <select
                        className="w-full px-2.5 sm:px-3 py-1.5 sm:py-2 bg-white dark:bg-navy-800 border border-navy-300 dark:border-navy-600 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 dark:focus:ring-navy-400 text-navy-900 dark:text-white transition-shadow duration-200"
                        value={settings.algorithm}
                        onChange={handleAlgorithmChange}
                    >
                        {botSettingsData.algorithms.map((algo) => (
                            <option key={algo.id} value={algo.id}>
                                {algo.name}
                            </option>
                        ))}
                    </select>
                </div>

                {/* Parameters */}
                <div className="grid grid-cols-2 gap-3 sm:gap-4">
                    <div>
                        <label className="block text-xs sm:text-sm font-medium text-navy-700 dark:text-navy-300 mb-1.5 sm:mb-2">
                            기간 (일)
                        </label>
                        <input
                            type="number"
                            className="w-full px-2.5 sm:px-3 py-1.5 sm:py-2 bg-white dark:bg-navy-800 border border-navy-300 dark:border-navy-600 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 dark:focus:ring-navy-400 text-navy-900 dark:text-white transition-shadow duration-200"
                            value={settings.params.period}
                            onChange={handleParamChange('period')}
                        />
                    </div>
                    <div>
                        <label className="block text-xs sm:text-sm font-medium text-navy-700 dark:text-navy-300 mb-1.5 sm:mb-2">
                            매매 비율 (%)
                        </label>
                        <input
                            type="number"
                            className="w-full px-2.5 sm:px-3 py-1.5 sm:py-2 bg-white dark:bg-navy-800 border border-navy-300 dark:border-navy-600 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 dark:focus:ring-navy-400 text-navy-900 dark:text-white transition-shadow duration-200"
                            value={settings.params.tradeRatio}
                            onChange={handleParamChange('tradeRatio')}
                        />
                    </div>
                    <div>
                        <label className="block text-xs sm:text-sm font-medium text-navy-700 dark:text-navy-300 mb-1.5 sm:mb-2">
                            손절 비율 (%)
                        </label>
                        <input
                            type="number"
                            className="w-full px-2.5 sm:px-3 py-1.5 sm:py-2 bg-white dark:bg-navy-800 border border-navy-300 dark:border-navy-600 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 dark:focus:ring-navy-400 text-navy-900 dark:text-white transition-shadow duration-200"
                            value={settings.params.stopLoss}
                            onChange={handleParamChange('stopLoss')}
                        />
                    </div>
                    <div>
                        <label className="block text-xs sm:text-sm font-medium text-navy-700 dark:text-navy-300 mb-1.5 sm:mb-2">
                            익절 비율 (%)
                        </label>
                        <input
                            type="number"
                            className="w-full px-2.5 sm:px-3 py-1.5 sm:py-2 bg-white dark:bg-navy-800 border border-navy-300 dark:border-navy-600 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-navy-500 dark:focus:ring-navy-400 text-navy-900 dark:text-white transition-shadow duration-200"
                            value={settings.params.takeProfit}
                            onChange={handleParamChange('takeProfit')}
                        />
                    </div>
                </div>
                <div className="flex justify-center">
                    <button
                        onClick={handleSave}
                        className="mt-6 w-2/3 md:w-1/2 lg:w-1/3 px-4 py-2 bg-navy-500 hover:bg-navy-600 text-white text-sm rounded-lg transition-all duration-200 shadow-lg shadow-navy-500/30"
                    >
                        저장
                    </button>
                </div>
            </div>

            {/* Backtesting Results */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-base sm:text-lg font-medium text-navy-900 dark:text-white mb-3 sm:mb-4">
                    백테스팅 결과
                </h3>
                <div className="grid grid-cols-2 gap-2 sm:gap-4">
                    <div className="p-2.5 sm:p-4 bg-white/50 dark:bg-navy-700/50 rounded-lg backdrop-blur-sm">
                        <p className="text-xs sm:text-sm text-navy-600 dark:text-navy-300">CAGR</p>
                        <p className="text-base sm:text-xl font-bold text-navy-900 dark:text-white">
                            {backtestData.results.cagr}%
                        </p>
                    </div>
                    <div className="p-2.5 sm:p-4 bg-white/50 dark:bg-navy-700/50 rounded-lg backdrop-blur-sm">
                        <p className="text-xs sm:text-sm text-navy-600 dark:text-navy-300">MDD</p>
                        <p className="text-base sm:text-xl font-bold text-navy-900 dark:text-white">
                            {backtestData.results.mdd}%
                        </p>
                    </div>
                    <div className="p-2.5 sm:p-4 bg-white/50 dark:bg-navy-700/50 rounded-lg backdrop-blur-sm">
                        <p className="text-xs sm:text-sm text-navy-600 dark:text-navy-300">승률</p>
                        <p className="text-base sm:text-xl font-bold text-navy-900 dark:text-white">
                            {backtestData.results.winRate}%
                        </p>
                    </div>
                    <div className="p-2.5 sm:p-4 bg-white/50 dark:bg-navy-700/50 rounded-lg backdrop-blur-sm">
                        <p className="text-xs sm:text-sm text-navy-600 dark:text-navy-300">
                            수익 팩터
                        </p>
                        <p className="text-base sm:text-xl font-bold text-navy-900 dark:text-white">
                            {backtestData.results.profitFactor}
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}
