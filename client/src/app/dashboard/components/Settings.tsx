import { useState, useEffect } from 'react';
import { BotSettingsData, BacktestData } from '../types';
import { FaChartLine, FaChartArea, FaCrosshairs, FaBitcoin, FaChevronUp, FaChevronDown, FaSave, FaSpinner } from 'react-icons/fa';
import { 
    MOCK_DATA, 
    BASE_ORDER_AMOUNT, 
    MAX_ORDER_AMOUNT, 
    CANDLE_INTERVALS,
    MARKETS 
} from '../constants';
import { userApi } from '@/api/services/userApi';
import { BotSettingRequest } from '@/api/types/user';
import { tradeApi } from '@/api/services/tradeApi';
import { StrategyResponse } from '@/api/types/trade';

const inputStyle = `
    w-full px-3 py-2 border border-navy-300 dark:border-navy-600 rounded-md 
    focus:outline-none focus:ring-2 focus:ring-navy-500 
    dark:bg-navy-800 dark:text-white
    [appearance:textfield]
    [&::-webkit-outer-spin-button]:appearance-none
    [&::-webkit-inner-spin-button]:appearance-none
`;

const spinButtonStyle = `
    flex items-center justify-center w-4 h-4
    bg-navy-100 hover:bg-navy-200
    dark:bg-navy-700 dark:hover:bg-navy-600
    text-navy-600 dark:text-navy-300
    transition-colors duration-200
    rounded text-xs
`;

const timeInputStyle = `
    w-full px-3 py-2 border border-navy-300 dark:border-navy-600 rounded-md 
    focus:outline-none focus:ring-2 focus:ring-navy-500 
    dark:bg-navy-800 dark:text-white
    [&::-webkit-calendar-picker-indicator]:filter
    [&::-webkit-calendar-picker-indicator]:dark:invert
    [&::-webkit-calendar-picker-indicator]:dark:brightness-0
    [&::-webkit-calendar-picker-indicator]:dark:contrast-200
`;

const selectInputStyle = `
    w-full px-3 py-2 pr-8 border border-navy-300 dark:border-navy-600 rounded-md 
    focus:outline-none focus:ring-2 focus:ring-navy-500 
    dark:bg-navy-800 dark:text-white
    appearance-none
`;



export default function Settings() {
    const [botSettingsData] = useState<BotSettingsData>(MOCK_DATA.botSettings);
    const [backtestData] = useState<BacktestData>(MOCK_DATA.backtest);
    const [strategies, setStrategies] = useState<StrategyResponse[]>([]);

    // Bot settings state
    const [botSetting, setBotSetting] = useState<BotSettingRequest>({
        botSettingId: '',
        strategy: 'STOCH_RSI',
        marketList: ['BTC'],
        baseOrderAmount: BASE_ORDER_AMOUNT,
        maxOrderAmount: MAX_ORDER_AMOUNT,
        startTime: null,
        endTime: null,
        candleInterval: 'THREE',
        candleCount: 100,
    });
    
    const [isLoading, setIsLoading] = useState(false);
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    // Load bot settings on component mount
    useEffect(() => {
        loadBotSettings();
        loadStrategies();
    }, []);

    const loadStrategies = async () => {
        const response = await tradeApi.getStrategies();
        setStrategies(response);
    };

    const loadBotSettings = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const response = await userApi.getBotSetting();
            
            // ISO 문자열을 시간 형식으로 변환
            const formatTimeFromISO = (isoString: string | null) => {
                if (!isoString) return null;
                const date = new Date(isoString);
                return date.toTimeString().slice(0, 5); // HH:mm 형식으로 변환
            };
            
            setBotSetting({
                botSettingId: response.botSettingId,
                strategy: response.strategy,
                marketList: response.marketList,
                baseOrderAmount: response.baseOrderAmount,
                maxOrderAmount: response.maxOrderAmount,
                startTime: formatTimeFromISO(response.startTime) || '09:00',
                endTime: formatTimeFromISO(response.endTime),
                candleInterval: response.candleInterval,
                candleCount: response.candleCount,
            });
        } catch (err) {
            console.error('Failed to load bot settings:', err);
            setError('봇 설정을 불러오는데 실패했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    const saveBotSettings = async () => {
        setIsSaving(true);
        setError(null);
        setSuccess(null);
        
        try {
            // 시간 형식을 ISO 문자열로 변환
            const formatTimeToISO = (timeString: string | null) => {
                if (!timeString) return null;
                const today = new Date();
                const [hours, minutes] = timeString.split(':');
                today.setHours(parseInt(hours), parseInt(minutes), 0, 0);
                return today.toISOString();
            };
            
            const requestData = {
                ...botSetting,
                startTime: formatTimeToISO(botSetting.startTime) || '09:00',
                endTime: formatTimeToISO(botSetting.endTime),
            };
            
            await userApi.updateBotSetting(requestData);
            setSuccess('SUCCESS SAVE');
            alert("봇 설정이 성공적으로 저장되었습니다!");
        } catch (err) {
            console.error('Failed to save bot settings:', err);
            setError('봇 설정 저장에 실패했습니다.');
        } finally {
            setIsSaving(false);
        }
    };

    const handleIncrement = (value: number, setValue: (value: number) => void, max: number) => {
        setValue(Math.min(value + 1, max));
    };

    const handleDecrement = (value: number, setValue: (value: number) => void, min: number) => {
        setValue(Math.max(value - 1, min));
    };

    const handleMarketToggle = (market: string) => {
        setBotSetting(prev => ({
            ...prev,
            marketList: prev.marketList.includes(market)
                ? prev.marketList.filter(m => m !== market)
                : [...prev.marketList, market]
        }));
    };

    if (isLoading) {
        return (
            <div className="flex items-center justify-center h-64">
                <FaSpinner className="animate-spin text-2xl text-navy-500" />
                <span className="ml-2 text-navy-700 dark:text-navy-300">봇 설정을 불러오는 중...</span>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* 알림 메시지 */}
            {error && (
                <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-300 px-4 py-3 rounded-lg">
                    {error}
                </div>
            )}
            {success && (
                <div className="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 text-green-700 dark:text-green-300 px-4 py-3 rounded-lg">
                    {success}
                </div>
            )}

            {/* 전략 선택 */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">전략 선택</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {strategies.map((strategy) => (
                        <button
                            key={strategy.name}
                            onClick={() => setBotSetting(prev => ({ ...prev, strategy: strategy.name }))}
                            className={`p-4 rounded-lg text-left transition-all duration-200 ${
                                botSetting.strategy === strategy.name
                                    ? 'bg-navy-100 dark:bg-navy-700 border-2 border-navy-500'
                                    : 'bg-navy-50/50 dark:bg-navy-800/50 border-2 border-transparent hover:border-navy-300'
                            }`}
                        >
                            <h4 className="text-base font-medium text-navy-900 dark:text-white mb-2">
                                {strategy.value}
                            </h4>
                            <p className="text-sm text-navy-600 dark:text-navy-300">
                                {strategy.description}
                            </p>
                        </button>
                    ))}
                </div>
            </div>

            {/* 거래소 마켓 선택 */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">거래소 마켓 선택</h3>
                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-2">
                    {MARKETS.map((market) => (
                        <button
                            key={market}
                            onClick={() => handleMarketToggle(market)}
                            className={`p-3 rounded-lg text-sm font-medium transition-all duration-200 ${
                                botSetting.marketList.includes(market)
                                    ? 'bg-navy-500 text-white'
                                    : 'bg-navy-50 dark:bg-navy-800 text-navy-700 dark:text-navy-300 hover:bg-navy-100 dark:hover:bg-navy-700'
                            }`}
                        >
                            {market}
                        </button>
                    ))}
                </div>
            </div>

            {/* 거래 설정 */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">거래 설정</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            기본 주문 금액 (원)
                        </label>
                        <div className="relative">
                            <input
                                type="text"
                                value={botSetting.baseOrderAmount.toLocaleString()}
                                onChange={(e) => {
                                    const rawValue = parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                    setBotSetting(prev => ({ ...prev, baseOrderAmount: rawValue }));
                                }}
                                onBlur={(e) => {
                                    const rawValue = parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                    if (rawValue < BASE_ORDER_AMOUNT) {
                                        setBotSetting(prev => ({ ...prev, baseOrderAmount: BASE_ORDER_AMOUNT }));
                                    } else if (rawValue > MAX_ORDER_AMOUNT) {
                                        setBotSetting(prev => ({ ...prev, baseOrderAmount: MAX_ORDER_AMOUNT }));
                                    }
                                }}
                                className={`${inputStyle} pr-12`}
                                placeholder={BASE_ORDER_AMOUNT.toLocaleString()}
                            />
                            <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                <button
                                    onClick={() => handleIncrement(botSetting.baseOrderAmount, (value) => setBotSetting(prev => ({ ...prev, baseOrderAmount: value })), MAX_ORDER_AMOUNT)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronUp size={8} />
                                </button>
                                <button
                                    onClick={() => handleDecrement(botSetting.baseOrderAmount, (value) => setBotSetting(prev => ({ ...prev, baseOrderAmount: value })), BASE_ORDER_AMOUNT)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronDown size={8} />
                                </button>
                            </div>
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            최대 주문 금액 (원)
                        </label>
                        <div className="relative">
                            <input
                                type="text"
                                value={botSetting.maxOrderAmount.toLocaleString()}
                                onChange={(e) => {
                                    const rawValue = parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                    setBotSetting(prev => ({ ...prev, maxOrderAmount: rawValue }));
                                }}
                                onBlur={(e) => {
                                    const rawValue = parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                    if (rawValue < BASE_ORDER_AMOUNT) {
                                        setBotSetting(prev => ({ ...prev, maxOrderAmount: BASE_ORDER_AMOUNT }));
                                    } else if (rawValue > MAX_ORDER_AMOUNT) {
                                        setBotSetting(prev => ({ ...prev, maxOrderAmount: MAX_ORDER_AMOUNT }));
                                    }
                                }}
                                className={`${inputStyle} pr-12`}
                                placeholder={MAX_ORDER_AMOUNT.toLocaleString()}
                            />
                            <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                <button
                                    onClick={() => handleIncrement(botSetting.maxOrderAmount, (value) => setBotSetting(prev => ({ ...prev, maxOrderAmount: value })), MAX_ORDER_AMOUNT)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronUp size={8} />
                                </button>
                                <button
                                    onClick={() => handleDecrement(botSetting.maxOrderAmount, (value) => setBotSetting(prev => ({ ...prev, maxOrderAmount: value })), BASE_ORDER_AMOUNT)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronDown size={8} />
                                </button>
                            </div>
                        </div>
                    </div>
                    {/* <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            거래 시작 시간
                        </label>
                        <input
                            type="time"
                            value={botSetting.startTime || ''}
                            onChange={(e) => setBotSetting(prev => ({ ...prev, startTime: e.target.value }))}
                            className={timeInputStyle}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            거래 종료 시간
                        </label>
                        <input
                            type="time"
                            value={botSetting.endTime || ''}
                            onChange={(e) => setBotSetting(prev => ({ ...prev, endTime: e.target.value }))}
                            className={timeInputStyle}
                        />
                    </div> */}
                </div>
            </div>

            {/* 차트 설정 */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">차트 설정</h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            캔들 간격
                        </label>
                        <select
                            value={botSetting.candleInterval}
                            onChange={(e) => setBotSetting(prev => ({ ...prev, candleInterval: e.target.value }))}
                            className={selectInputStyle}
                        >
                            {CANDLE_INTERVALS.map((interval) => (
                                <option key={interval.value} value={interval.value}>
                                    {interval.label}
                                </option>
                            ))}
                        </select>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            캔들 개수
                        </label>
                        <div className="relative">
                            <input
                                type="text"
                                value={botSetting.candleCount}
                                onChange={(e) => {
                                    const rawValue = parseInt(e.target.value, 10) || 0;
                                    setBotSetting(prev => ({ ...prev, candleCount: rawValue }));
                                }}
                                onBlur={(e) => {
                                    const rawValue = parseInt(e.target.value, 10) || 0;
                                    if (rawValue < 10) {
                                        setBotSetting(prev => ({ ...prev, candleCount: 10 }));
                                    } else if (rawValue > 200) {
                                        setBotSetting(prev => ({ ...prev, candleCount: 200 }));
                                    }
                                }}
                                className={`${inputStyle} pr-12`}
                                placeholder="100"
                            />
                            <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                <button
                                    onClick={() => handleIncrement(botSetting.candleCount, (value) => setBotSetting(prev => ({ ...prev, candleCount: value })), 200)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronUp size={8} />
                                </button>
                                <button
                                    onClick={() => handleDecrement(botSetting.candleCount, (value) => setBotSetting(prev => ({ ...prev, candleCount: value })), 10)}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronDown size={8} />
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* 설정 저장 버튼 */}
            <div className="flex justify-end bg-transparent pr-4">
                <button 
                    onClick={saveBotSettings}
                    disabled={isSaving}
                    className="w-full sm:w-auto px-6 py-2 bg-navy-500 hover:bg-navy-600 disabled:bg-navy-400 text-white text-sm rounded-lg transition-colors duration-200 shadow-lg shadow-navy-500/30 flex items-center justify-center gap-2"
                >
                    {isSaving ? (
                        <>
                            <FaSpinner className="animate-spin" />
                            저장 중...
                        </>
                    ) : (
                        <>
                            <FaSave />
                            설정 저장
                        </>
                    )}
                </button>
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
