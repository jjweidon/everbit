import { useState, useEffect } from 'react';
import { BacktestData } from '../types';
import {
    FaChartLine,
    FaChartArea,
    FaCrosshairs,
    FaBitcoin,
    FaChevronUp,
    FaChevronDown,
    FaSave,
    FaSpinner,
} from 'react-icons/fa';
import { MOCK_DATA, BASE_ORDER_AMOUNT, MAX_ORDER_AMOUNT, MARKETS } from '../constants';
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
    const [backtestData] = useState<BacktestData>(MOCK_DATA.backtest);
    const [strategies, setStrategies] = useState<StrategyResponse[]>([]);

    // Bot settings state
    const [botSetting, setBotSetting] = useState<BotSettingRequest>({
        botSettingId: '',
        buyStrategy: 'TRIPLE_INDICATOR_MODERATE',
        sellStrategy: 'TRIPLE_INDICATOR_MODERATE',
        marketList: ['BTC'],
        buyBaseOrderAmount: BASE_ORDER_AMOUNT,
        buyMaxOrderAmount: MAX_ORDER_AMOUNT,
        sellBaseOrderAmount: BASE_ORDER_AMOUNT,
        sellMaxOrderAmount: MAX_ORDER_AMOUNT,
        lossThreshold: 0.01,
        profitThreshold: 0.018,
        lossSellRatio: 0.9,
        profitSellRatio: 0.5,
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

            setBotSetting({
                botSettingId: response.botSettingId,
                buyStrategy: response.buyStrategy,
                sellStrategy: response.sellStrategy,
                marketList: response.marketList,
                buyBaseOrderAmount: response.buyBaseOrderAmount,
                buyMaxOrderAmount: response.buyMaxOrderAmount,
                sellBaseOrderAmount: response.sellBaseOrderAmount,
                sellMaxOrderAmount: response.sellMaxOrderAmount,
                lossThreshold: response.lossThreshold,
                profitThreshold: response.profitThreshold,
                lossSellRatio: response.lossSellRatio,
                profitSellRatio: response.profitSellRatio,
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
            const requestData = {
                ...botSetting,
            };

            await userApi.updateBotSetting(requestData);
            setSuccess('SUCCESS SAVE');
            alert('봇 설정이 성공적으로 저장되었습니다!');
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
        setBotSetting((prev) => ({
            ...prev,
            marketList: prev.marketList.includes(market)
                ? prev.marketList.filter((m) => m !== market)
                : [...prev.marketList, market],
        }));
    };

    if (isLoading) {
        return (
            <div className="flex items-center justify-center h-64">
                <FaSpinner className="animate-spin text-2xl text-navy-500" />
                <span className="ml-2 text-navy-700 dark:text-navy-300">
                    봇 설정을 불러오는 중...
                </span>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex gap-4">
                {/* 매수 전략 선택 */}
                <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                    <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">
                        매수 전략 선택
                    </h3>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        {strategies.map((strategy) => (
                            <button
                                key={strategy.name}
                                onClick={() =>
                                    setBotSetting((prev) => ({
                                        ...prev,
                                        buyStrategy: strategy.name,
                                    }))
                                }
                                className={`p-4 rounded-lg text-left transition-all duration-200 ${
                                    botSetting.buyStrategy === strategy.name
                                        ? 'bg-navy-100 dark:bg-navy-700 border-2 border-navy-500'
                                        : 'bg-navy-50/50 dark:bg-navy-800/50 border-2 border-transparent hover:border-navy-300'
                                }`}
                            >
                                <h4 className="text-base font-medium text-navy-900 dark:text-white mb-2">
                                    {strategy.value}
                                </h4>
                                <p className="text-[0.6rem] text-navy-600 dark:text-navy-300">
                                    {strategy.description}
                                </p>
                            </button>
                        ))}
                    </div>
                </div>

                {/* 매도 전략 선택 */}
                <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                    <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">
                        매도 전략 선택
                    </h3>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        {strategies.map((strategy) => (
                            <button
                                key={strategy.name}
                                onClick={() =>
                                    setBotSetting((prev) => ({
                                        ...prev,
                                        sellStrategy: strategy.name,
                                    }))
                                }
                                className={`p-4 rounded-lg text-left transition-all duration-200 ${
                                    botSetting.sellStrategy === strategy.name
                                        ? 'bg-navy-100 dark:bg-navy-700 border-2 border-navy-500'
                                        : 'bg-navy-50/50 dark:bg-navy-800/50 border-2 border-transparent hover:border-navy-300'
                                }`}
                            >
                                <h4 className="text-base font-medium text-navy-900 dark:text-white mb-2">
                                    {strategy.value}
                                </h4>
                                <p className="text-[0.6rem] text-navy-600 dark:text-navy-300">
                                    {strategy.description}
                                </p>
                            </button>
                        ))}
                    </div>
                </div>
            </div>

            {/* 거래소 마켓 선택 */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">
                    거래소 마켓 선택
                </h3>
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
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">
                    거래 설정
                </h3>
                
                {/* 매수 설정 */}
                <div className="mb-6">
                    <h4 className="text-md font-medium text-navy-800 dark:text-navy-200 mb-3 border-b border-navy-200 dark:border-navy-600 pb-2">
                        매수 설정
                    </h4>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                                매수 최소 주문 금액 (원)
                            </label>
                            <div className="relative">
                                <input
                                    type="text"
                                    value={botSetting.buyBaseOrderAmount.toLocaleString()}
                                    onChange={(e) => {
                                        const rawValue =
                                            parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        setBotSetting((prev) => ({
                                            ...prev,
                                            buyBaseOrderAmount: rawValue,
                                        }));
                                    }}
                                    onBlur={(e) => {
                                        const rawValue =
                                            parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        if (rawValue < BASE_ORDER_AMOUNT) {
                                            setBotSetting((prev) => ({
                                                ...prev,
                                                buyBaseOrderAmount: BASE_ORDER_AMOUNT,
                                            }));
                                        } else if (rawValue > MAX_ORDER_AMOUNT) {
                                            setBotSetting((prev) => ({
                                                ...prev,
                                                buyBaseOrderAmount: MAX_ORDER_AMOUNT,
                                            }));
                                        }
                                    }}
                                    className={`${inputStyle} pr-12`}
                                    placeholder={BASE_ORDER_AMOUNT.toLocaleString()}
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() =>
                                            handleIncrement(
                                                botSetting.buyBaseOrderAmount,
                                                (value) =>
                                                    setBotSetting((prev) => ({
                                                        ...prev,
                                                        buyBaseOrderAmount: value,
                                                    })),
                                                MAX_ORDER_AMOUNT
                                            )
                                        }
                                        className={spinButtonStyle}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() =>
                                            handleDecrement(
                                                botSetting.buyBaseOrderAmount,
                                                (value) =>
                                                    setBotSetting((prev) => ({
                                                        ...prev,
                                                        buyBaseOrderAmount: value,
                                                    })),
                                                BASE_ORDER_AMOUNT
                                            )
                                        }
                                        className={spinButtonStyle}
                                    >
                                        <FaChevronDown size={8} />
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                                매수 최대 주문 금액 (원)
                            </label>
                            <div className="relative">
                                <input
                                    type="text"
                                    value={botSetting.buyMaxOrderAmount.toLocaleString()}
                                    onChange={(e) => {
                                        const rawValue =
                                            parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        setBotSetting((prev) => ({
                                            ...prev,
                                            buyMaxOrderAmount: rawValue,
                                        }));
                                    }}
                                    onBlur={(e) => {
                                        const rawValue =
                                            parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        if (rawValue < BASE_ORDER_AMOUNT) {
                                            setBotSetting((prev) => ({
                                                ...prev,
                                                buyMaxOrderAmount: BASE_ORDER_AMOUNT,
                                            }));
                                        } else if (rawValue > MAX_ORDER_AMOUNT) {
                                            setBotSetting((prev) => ({
                                                ...prev,
                                                buyMaxOrderAmount: MAX_ORDER_AMOUNT,
                                            }));
                                        }
                                    }}
                                    className={`${inputStyle} pr-12`}
                                    placeholder={MAX_ORDER_AMOUNT.toLocaleString()}
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() =>
                                            handleIncrement(
                                                botSetting.buyMaxOrderAmount,
                                                (value) =>
                                                    setBotSetting((prev) => ({
                                                        ...prev,
                                                        buyMaxOrderAmount: value,
                                                    })),
                                                MAX_ORDER_AMOUNT
                                            )
                                        }
                                        className={spinButtonStyle}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() =>
                                            handleDecrement(
                                                botSetting.buyMaxOrderAmount,
                                                (value) =>
                                                    setBotSetting((prev) => ({
                                                        ...prev,
                                                        buyMaxOrderAmount: value,
                                                    })),
                                                BASE_ORDER_AMOUNT
                                            )
                                        }
                                        className={spinButtonStyle}
                                    >
                                        <FaChevronDown size={8} />
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 매도 설정 */}
                <div className="mb-6">
                    <h4 className="text-md font-medium text-navy-800 dark:text-navy-200 mb-3 border-b border-navy-200 dark:border-navy-600 pb-2">
                        매도 설정
                    </h4>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                                매도 최소 주문 금액 (원)
                            </label>
                            <div className="relative">
                                <input
                                    type="text"
                                    value={botSetting.sellBaseOrderAmount.toLocaleString()}
                                    onChange={(e) => {
                                        const rawValue =
                                            parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        setBotSetting((prev) => ({
                                            ...prev,
                                            sellBaseOrderAmount: rawValue,
                                        }));
                                    }}
                                    onBlur={(e) => {
                                        const rawValue =
                                            parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        if (rawValue < BASE_ORDER_AMOUNT) {
                                            setBotSetting((prev) => ({
                                                ...prev,
                                                sellBaseOrderAmount: BASE_ORDER_AMOUNT,
                                            }));
                                        } else if (rawValue > MAX_ORDER_AMOUNT) {
                                            setBotSetting((prev) => ({
                                                ...prev,
                                                sellBaseOrderAmount: MAX_ORDER_AMOUNT,
                                            }));
                                        }
                                    }}
                                    className={`${inputStyle} pr-12`}
                                    placeholder={BASE_ORDER_AMOUNT.toLocaleString()}
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() =>
                                            handleIncrement(
                                                botSetting.sellBaseOrderAmount,
                                                (value) =>
                                                    setBotSetting((prev) => ({
                                                        ...prev,
                                                        sellBaseOrderAmount: value,
                                                    })),
                                                MAX_ORDER_AMOUNT
                                            )
                                        }
                                        className={spinButtonStyle}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() =>
                                            handleDecrement(
                                                botSetting.sellBaseOrderAmount,
                                                (value) =>
                                                    setBotSetting((prev) => ({
                                                        ...prev,
                                                        sellBaseOrderAmount: value,
                                                    })),
                                                BASE_ORDER_AMOUNT
                                            )
                                        }
                                        className={spinButtonStyle}
                                    >
                                        <FaChevronDown size={8} />
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                                매도 최대 주문 금액 (원)
                            </label>
                            <div className="relative">
                                <input
                                    type="text"
                                    value={botSetting.sellMaxOrderAmount.toLocaleString()}
                                    onChange={(e) => {
                                        const rawValue =
                                            parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        setBotSetting((prev) => ({
                                            ...prev,
                                            sellMaxOrderAmount: rawValue,
                                        }));
                                    }}
                                    onBlur={(e) => {
                                        const rawValue =
                                            parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        if (rawValue < BASE_ORDER_AMOUNT) {
                                            setBotSetting((prev) => ({
                                                ...prev,
                                                sellMaxOrderAmount: BASE_ORDER_AMOUNT,
                                            }));
                                        } else if (rawValue > MAX_ORDER_AMOUNT) {
                                            setBotSetting((prev) => ({
                                                ...prev,
                                                sellMaxOrderAmount: MAX_ORDER_AMOUNT,
                                            }));
                                        }
                                    }}
                                    className={`${inputStyle} pr-12`}
                                    placeholder={MAX_ORDER_AMOUNT.toLocaleString()}
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() =>
                                            handleIncrement(
                                                botSetting.sellMaxOrderAmount,
                                                (value) =>
                                                    setBotSetting((prev) => ({
                                                        ...prev,
                                                        sellMaxOrderAmount: value,
                                                    })),
                                                MAX_ORDER_AMOUNT
                                            )
                                        }
                                        className={spinButtonStyle}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() =>
                                            handleDecrement(
                                                botSetting.sellMaxOrderAmount,
                                                (value) =>
                                                    setBotSetting((prev) => ({
                                                        ...prev,
                                                        sellMaxOrderAmount: value,
                                                    })),
                                                BASE_ORDER_AMOUNT
                                            )
                                        }
                                        className={spinButtonStyle}
                                    >
                                        <FaChevronDown size={8} />
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* 손실/이익 관리 설정 */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">
                    손실/이익 관리 설정
                </h3>
                
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {/* 손실 임계값 */}
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            손실 임계값 (%)
                        </label>
                        <div className="relative">
                            <input
                                type="number"
                                step="0.1"
                                min="0"
                                max="100"
                                value={(botSetting.lossThreshold * 100).toFixed(1)}
                                onChange={(e) => {
                                    const value = parseFloat(e.target.value) / 100 || 0;
                                    setBotSetting((prev) => ({
                                        ...prev,
                                        lossThreshold: value,
                                    }));
                                }}
                                className={`${inputStyle} pr-12`}
                                placeholder="1.0"
                            />
                            <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                <button
                                    onClick={() => {
                                        const newValue = Math.min(botSetting.lossThreshold + 0.001, 1);
                                        setBotSetting((prev) => ({
                                            ...prev,
                                            lossThreshold: newValue,
                                        }));
                                    }}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronUp size={8} />
                                </button>
                                <button
                                    onClick={() => {
                                        const newValue = Math.max(botSetting.lossThreshold - 0.001, 0);
                                        setBotSetting((prev) => ({
                                            ...prev,
                                            lossThreshold: newValue,
                                        }));
                                    }}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronDown size={8} />
                                </button>
                            </div>
                        </div>
                        <p className="text-xs text-navy-500 dark:text-navy-400 mt-1">
                            이 비율 이상 손실 시 자동 매도 실행
                        </p>
                    </div>

                    {/* 이익 임계값 */}
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            이익 임계값 (%)
                        </label>
                        <div className="relative">
                            <input
                                type="number"
                                step="0.1"
                                min="0"
                                max="100"
                                value={(botSetting.profitThreshold * 100).toFixed(1)}
                                onChange={(e) => {
                                    const value = parseFloat(e.target.value) / 100 || 0;
                                    setBotSetting((prev) => ({
                                        ...prev,
                                        profitThreshold: value,
                                    }));
                                }}
                                className={`${inputStyle} pr-12`}
                                placeholder="1.8"
                            />
                            <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                <button
                                    onClick={() => {
                                        const newValue = Math.min(botSetting.profitThreshold + 0.001, 1);
                                        setBotSetting((prev) => ({
                                            ...prev,
                                            profitThreshold: newValue,
                                        }));
                                    }}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronUp size={8} />
                                </button>
                                <button
                                    onClick={() => {
                                        const newValue = Math.max(botSetting.profitThreshold - 0.001, 0);
                                        setBotSetting((prev) => ({
                                            ...prev,
                                            profitThreshold: newValue,
                                        }));
                                    }}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronDown size={8} />
                                </button>
                            </div>
                        </div>
                        <p className="text-xs text-navy-500 dark:text-navy-400 mt-1">
                            이 비율 이상 이익 시 자동 매도 실행
                        </p>
                    </div>

                    {/* 손실 매도 비율 */}
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            손실 매도 비율 (%)
                        </label>
                        <div className="relative">
                            <input
                                type="number"
                                step="1"
                                min="0"
                                max="100"
                                value={(botSetting.lossSellRatio * 100).toFixed(0)}
                                onChange={(e) => {
                                    const value = parseInt(e.target.value) / 100 || 0;
                                    setBotSetting((prev) => ({
                                        ...prev,
                                        lossSellRatio: value,
                                    }));
                                }}
                                className={`${inputStyle} pr-12`}
                                placeholder="90"
                            />
                            <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                <button
                                    onClick={() => {
                                        const newValue = Math.min(botSetting.lossSellRatio + 0.01, 1);
                                        setBotSetting((prev) => ({
                                            ...prev,
                                            lossSellRatio: newValue,
                                        }));
                                    }}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronUp size={8} />
                                </button>
                                <button
                                    onClick={() => {
                                        const newValue = Math.max(botSetting.lossSellRatio - 0.01, 0);
                                        setBotSetting((prev) => ({
                                            ...prev,
                                            lossSellRatio: newValue,
                                        }));
                                    }}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronDown size={8} />
                                </button>
                            </div>
                        </div>
                        <p className="text-xs text-navy-500 dark:text-navy-400 mt-1">
                            손실 시 보유량의 몇 %를 매도할지 설정
                        </p>
                    </div>

                    {/* 이익 매도 비율 */}
                    <div>
                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                            이익 매도 비율 (%)
                        </label>
                        <div className="relative">
                            <input
                                type="number"
                                step="1"
                                min="0"
                                max="100"
                                value={(botSetting.profitSellRatio * 100).toFixed(0)}
                                onChange={(e) => {
                                    const value = parseInt(e.target.value) / 100 || 0;
                                    setBotSetting((prev) => ({
                                        ...prev,
                                        profitSellRatio: value,
                                    }));
                                }}
                                className={`${inputStyle} pr-12`}
                                placeholder="50"
                            />
                            <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                <button
                                    onClick={() => {
                                        const newValue = Math.min(botSetting.profitSellRatio + 0.01, 1);
                                        setBotSetting((prev) => ({
                                            ...prev,
                                            profitSellRatio: newValue,
                                        }));
                                    }}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronUp size={8} />
                                </button>
                                <button
                                    onClick={() => {
                                        const newValue = Math.max(botSetting.profitSellRatio - 0.01, 0);
                                        setBotSetting((prev) => ({
                                            ...prev,
                                            profitSellRatio: newValue,
                                        }));
                                    }}
                                    className={spinButtonStyle}
                                >
                                    <FaChevronDown size={8} />
                                </button>
                            </div>
                        </div>
                        <p className="text-xs text-navy-500 dark:text-navy-400 mt-1">
                            이익 시 보유량의 몇 %를 매도할지 설정
                        </p>
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
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">
                    백테스트 결과
                </h3>
                <div className="grid grid-cols-2 gap-2 sm:gap-4">
                    <div className="p-2.5 sm:p-4 bg-white/50 dark:bg-navy-800/90 rounded-md backdrop-blur-sm">
                        <div className="flex justify-between items-center">
                            <div>
                                <p className="text-xs sm:text-sm text-navy-600 dark:text-navy-300">
                                    CAGR <span className="text-[0.6rem]">(연평균 성장률)</span>
                                </p>
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
                                <p className="text-xs sm:text-sm text-navy-600 dark:text-navy-300">
                                    MDD <span className="text-[0.6rem]">(최대 낙폭)</span>
                                </p>
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
                                <p className="text-xs sm:text-sm text-navy-600 dark:text-navy-300">
                                    승률
                                </p>
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
                                <p className="text-xs sm:text-sm text-navy-600 dark:text-navy-300">
                                    수익 팩터
                                </p>
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
