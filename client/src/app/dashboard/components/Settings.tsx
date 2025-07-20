import { useState } from 'react';
import { BotSettingsData, BacktestData } from '../types';
import { FaChartLine, FaChartArea, FaCrosshairs, FaBitcoin, FaChevronUp, FaChevronDown } from 'react-icons/fa';
import { MOCK_DATA } from '../constants';

const inputStyle = `
    w-full px-3 py-2 border border-navy-300 dark:border-navy-600 rounded-md 
    focus:outline-none focus:ring-2 focus:ring-navy-500 
    dark:bg-navy-800 dark:text-white
    [appearance:textfield]
    [&::-webkit-outer-spin-button]:appearance-none
    [&::-webkit-inner-spin-button]:appearance-none
`;

const spinButtonStyle = `
    flex items-center justify-center w-6 h-6
    bg-navy-100 hover:bg-navy-200
    dark:bg-navy-700 dark:hover:bg-navy-600
    text-navy-600 dark:text-navy-300
    transition-colors duration-200
    rounded
`;

export default function Settings() {
    const [botSettingsData] = useState<BotSettingsData>(MOCK_DATA.botSettings);
    const [backtestData] = useState<BacktestData>(MOCK_DATA.backtest);
    const [selectedAlgorithm, setSelectedAlgorithm] = useState(botSettingsData.currentSettings.algorithm);
    const [period, setPeriod] = useState(botSettingsData.currentSettings.params.period);
    const [tradeRatio, setTradeRatio] = useState(botSettingsData.currentSettings.params.tradeRatio);
    const [stopLoss, setStopLoss] = useState(botSettingsData.currentSettings.params.stopLoss);
    const [takeProfit, setTakeProfit] = useState(botSettingsData.currentSettings.params.takeProfit);

    const handleIncrement = (value: number, setValue: (value: number) => void, max: number) => {
        setValue(Math.min(value + 1, max));
    };

    const handleDecrement = (value: number, setValue: (value: number) => void, min: number) => {
        setValue(Math.max(value - 1, min));
    };

    return (
        <div className="space-y-6">
            {/* 알고리즘 선택 */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">알고리즘 선택</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {botSettingsData.algorithms.map((algorithm) => (
                        <button
                            key={algorithm.id}
                            onClick={() => setSelectedAlgorithm(algorithm.id)}
                            className={`p-4 rounded-lg text-left transition-all duration-200 ${
                                selectedAlgorithm === algorithm.id
                                    ? 'bg-navy-100 dark:bg-navy-700 border-2 border-navy-500'
                                    : 'bg-navy-50/50 dark:bg-navy-800/50 border-2 border-transparent hover:border-navy-300'
                            }`}
                        >
                            <h4 className="text-base font-medium text-navy-900 dark:text-white mb-2">
                                {algorithm.name}
                            </h4>
                            <p className="text-sm text-navy-600 dark:text-navy-300">
                                {algorithm.description}
                            </p>
                        </button>
                    ))}
                </div>
            </div>

            {/* 파라미터 설정 */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">파라미터 설정</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            기간
                        </label>
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                value={period}
                                min={1}
                                max={90}
                                onChange={(e) => setPeriod(Number(e.target.value))}
                                className={inputStyle}
                            />
                            <div className="flex flex-col gap-1">
                                <button
                                    onClick={() => handleIncrement(period, setPeriod, 90)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronUp size={12} />
                                </button>
                                <button
                                    onClick={() => handleDecrement(period, setPeriod, 1)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronDown size={12} />
                                </button>
                            </div>
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            투자 비율 (%)
                        </label>
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                value={tradeRatio}
                                min={1}
                                max={100}
                                onChange={(e) => setTradeRatio(Number(e.target.value))}
                                className={inputStyle}
                            />
                            <div className="flex flex-col gap-1">
                                <button
                                    onClick={() => handleIncrement(tradeRatio, setTradeRatio, 100)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronUp size={12} />
                                </button>
                                <button
                                    onClick={() => handleDecrement(tradeRatio, setTradeRatio, 1)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronDown size={12} />
                                </button>
                            </div>
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            손절 (%)
                        </label>
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                value={stopLoss}
                                min={1}
                                max={100}
                                onChange={(e) => setStopLoss(Number(e.target.value))}
                                className={inputStyle}
                            />
                            <div className="flex flex-col gap-1">
                                <button
                                    onClick={() => handleIncrement(stopLoss, setStopLoss, 100)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronUp size={12} />
                                </button>
                                <button
                                    onClick={() => handleDecrement(stopLoss, setStopLoss, 1)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronDown size={12} />
                                </button>
                            </div>
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            익절 (%)
                        </label>
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                value={takeProfit}
                                min={1}
                                max={100}
                                onChange={(e) => setTakeProfit(Number(e.target.value))}
                                className={inputStyle}
                            />
                            <div className="flex flex-col gap-1">
                                <button
                                    onClick={() => handleIncrement(takeProfit, setTakeProfit, 100)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronUp size={12} />
                                </button>
                                <button
                                    onClick={() => handleDecrement(takeProfit, setTakeProfit, 1)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronDown size={12} />
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="mt-6">
                    <button className="w-full sm:w-auto px-6 py-3 bg-navy-500 hover:bg-navy-600 text-white rounded-lg transition-colors duration-200 shadow-lg shadow-navy-500/30">
                        설정 저장
                    </button>
                </div>
            </div>

            {/* 백테스트 결과 */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">백테스트 결과</h3>
                <div className="grid grid-cols-2 gap-2 sm:gap-4">
                    <div className="p-2.5 sm:p-4 bg-white/50 dark:bg-navy-800/90 rounded-md backdrop-blur-sm">
                        <div className="flex justify-between items-center">
                            <div>
                                <p className="text-xs sm:text-sm text-navy-600 dark:text-navy-300">CAGR <span className="text-[0.6rem]">(연평균 성장률)</span></p>
                                <p className="text-base sm:text-xl font-bold text-navy-900 dark:text-white">
                                    {backtestData.results.cagr}%
                                </p>
                            </div>
                            <FaChartLine className="text-lg sm:text-xl text-navy-600 dark:text-navy-300" />
                        </div>
                    </div>
                    <div className="p-2.5 sm:p-4 bg-white/50 dark:bg-navy-800/90 rounded-md backdrop-blur-sm">
                        <div className="flex justify-between items-center">
                            <div>
                                <p className="text-xs sm:text-sm text-navy-600 dark:text-navy-300">MDD <span className="text-[0.6rem]">(최대 낙폭)</span></p>
                                <p className="text-base sm:text-xl font-bold text-navy-900 dark:text-white">
                                    {backtestData.results.mdd}%
                                </p>
                            </div>
                            <FaChartArea className="text-lg sm:text-xl text-navy-600 dark:text-navy-300" />
                        </div>
                    </div>
                    <div className="p-2.5 sm:p-4 bg-white/50 dark:bg-navy-800/90 rounded-md backdrop-blur-sm">
                        <div className="flex justify-between items-center">
                            <div>
                                <p className="text-xs sm:text-sm text-navy-600 dark:text-navy-300">승률</p>
                                <p className="text-base sm:text-xl font-bold text-green-500">
                                    {backtestData.results.winRate}%
                                </p>
                            </div>
                            <FaCrosshairs className="text-lg sm:text-xl text-navy-600 dark:text-navy-300" />
                        </div>
                    </div>
                    <div className="p-2.5 sm:p-4 bg-white/50 dark:bg-navy-800/90 rounded-md backdrop-blur-sm">
                        <div className="flex justify-between items-center">
                            <div>
                                <p className="text-xs sm:text-sm text-navy-600 dark:text-navy-300">수익 팩터</p>
                                <p className="text-base sm:text-xl font-bold text-navy-900 dark:text-white">
                                    {backtestData.results.profitFactor}
                                </p>
                            </div>
                            <FaBitcoin className="text-lg sm:text-xl text-navy-600 dark:text-navy-300" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
