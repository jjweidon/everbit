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
import { MOCK_DATA, BASE_ORDER_AMOUNT, MAX_ORDER_AMOUNT } from '../constants';
import { userApi } from '@/api/services/userApi';
import { BotSettingRequest } from '@/api/types/user';
import { tradeApi } from '@/api/services/tradeApi';
import { StrategyResponse, MarketResponse } from '@/api/types/trade';

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

export default function Settings() {
    const [backtestData] = useState<BacktestData>(MOCK_DATA.backtest);
    const [strategies, setStrategies] = useState<StrategyResponse[]>([]);
    const [markets, setMarkets] = useState<MarketResponse[]>([]);

    // Bot settings state - 초기값 없이 null로 시작
    const [botSetting, setBotSetting] = useState<BotSettingRequest | null>(null);

    const [isLoading, setIsLoading] = useState(true); // 초기 로딩 상태를 true로 설정
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    // Load bot settings on component mount
    useEffect(() => {
        loadBotSettings();
        loadStrategies();
        loadMarkets();
    }, []);

    const loadStrategies = async () => {
        const response = await tradeApi.getStrategies();
        setStrategies(response);
    };

    const loadMarkets = async () => {
        const response = await tradeApi.getMarkets();
        setMarkets(response);
    };

    const loadBotSettings = async () => {
        setError(null);
        try {
            const response = await userApi.getBotSetting();
            setBotSetting(response);
        } catch (err) {
            console.error('Failed to load bot settings:', err);
            setError('봇 설정을 불러오는데 실패했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    const saveBotSettings = async () => {
        if (!botSetting) return;
        
        setIsSaving(true);
        setError(null);
        setSuccess(null);

        try {
            const response = await userApi.updateBotSetting(botSetting);
            setBotSetting(response);
            setSuccess('SUCCESS SAVE');
            alert('봇 설정이 성공적으로 저장되었습니다!');
        } catch (err) {
            console.error('Failed to save bot settings:', err);
            setError('봇 설정 저장에 실패했습니다.');
        } finally {
            setIsSaving(false);
        }
    };

    // 봇 설정 업데이트 헬퍼 함수
    const updateBotSetting = <K extends keyof BotSettingRequest>(
        key: K,
        value: BotSettingRequest[K]
    ) => {
        setBotSetting((prev) => prev ? ({ ...prev, [key]: value }) : null);
    };

    const handleIncrement = (value: number, key: keyof BotSettingRequest, max: number) => {
        updateBotSetting(key, Math.min(value + 1, max) as any);
    };

    const handleDecrement = (value: number, key: keyof BotSettingRequest, min: number) => {
        updateBotSetting(key, Math.max(value - 1, min) as any);
    };

    const handleMarketToggle = (market: string) => {
        if (!botSetting) return;
        const newMarketList = botSetting.marketList.includes(market)
            ? botSetting.marketList.filter((m) => m !== market)
            : [...botSetting.marketList, market];
        updateBotSetting('marketList', newMarketList);
    };

    if (isLoading || !botSetting) {
        return (
            <div className="flex items-center justify-center h-64">
                <FaSpinner className="animate-spin text-3xl text-navy-500" />
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* 거래소 마켓 선택 */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">
                    거래소 마켓 선택
                </h3>
                <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-3">
                    {markets.map((market) => (
                        <button
                            key={market.market}
                            onClick={() => handleMarketToggle(market.market)}
                            className={`p-3 rounded-lg text-left transition-all duration-200 ${
                                botSetting.marketList.includes(market.market)
                                    ? 'bg-navy-500 text-white'
                                    : 'bg-navy-50 dark:bg-navy-800 text-navy-700 dark:text-navy-300 hover:bg-navy-100 dark:hover:bg-navy-700'
                            }`}
                        >
                            <div className="flex items-center justify-between">
                                <div>
                                    <div className="font-medium text-sm">
                                        {market.market}
                                    </div>
                                    <div className={`text-xs mt-0.5 ${
                                        botSetting.marketList.includes(market.market)
                                            ? 'text-navy-100'
                                            : 'text-navy-500 dark:text-navy-400'
                                    }`}>
                                        {market.description}
                                    </div>
                                </div>
                                <div className={`w-4 h-4 rounded-full border-2 flex items-center justify-center ${
                                    botSetting.marketList.includes(market.market)
                                        ? 'border-white bg-white'
                                        : 'border-navy-300 dark:border-navy-500'
                                }`}>
                                    {botSetting.marketList.includes(market.market) && (
                                        <div className="w-2 h-2 rounded-full bg-navy-500"></div>
                                    )}
                                </div>
                            </div>
                        </button>
                    ))}
                </div>
            </div>

            {/* 자동 매수/매도 주문 활성화 설정 */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">
                    자동 주문 활성화 설정
                </h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                    {/* 자동 매수 활성화 */}
                    <div className="flex items-center justify-between p-4 bg-navy-50/50 dark:bg-navy-800/50 rounded-lg">
                        <div>
                            <h4 className="text-base font-medium text-navy-800 dark:text-navy-200 mb-2">
                                자동 매수 활성화
                            </h4>
                            <p className="text-sm text-navy-600 dark:text-navy-400">
                                설정된 전략에 따라 자동으로 매수 주문을 실행합니다
                            </p>
                        </div>
                        <button
                            onClick={() => updateBotSetting('isBuyActive', !botSetting.isBuyActive)}
                            className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-navy-500 ${
                                botSetting.isBuyActive
                                    ? 'bg-green-500'
                                    : 'bg-gray-300 dark:bg-gray-600'
                            }`}
                        >
                            <span
                                className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200 ${
                                    botSetting.isBuyActive ? 'translate-x-6' : 'translate-x-1'
                                }`}
                            />
                        </button>
                    </div>

                    {/* 자동 매도 활성화 */}
                    <div className="flex items-center justify-between p-4 bg-navy-50/50 dark:bg-navy-800/50 rounded-lg">
                        <div>
                            <h4 className="text-base font-medium text-navy-800 dark:text-navy-200 mb-2">
                                자동 매도 활성화
                            </h4>
                            <p className="text-sm text-navy-600 dark:text-navy-400">
                                설정된 전략에 따라 자동으로 매도 주문을 실행합니다
                            </p>
                        </div>
                        <button
                            onClick={() => updateBotSetting('isSellActive', !botSetting.isSellActive)}
                            className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-navy-500 ${
                                botSetting.isSellActive
                                    ? 'bg-red-500'
                                    : 'bg-gray-300 dark:bg-gray-600'
                            }`}
                        >
                            <span
                                className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200 ${
                                    botSetting.isSellActive ? 'translate-x-6' : 'translate-x-1'
                                }`}
                            />
                        </button>
                    </div>
                </div>
            </div>

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
                                onClick={() => updateBotSetting('buyStrategy', strategy.name)}
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
                                onClick={() => updateBotSetting('sellStrategy', strategy.name)}
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
                                        const rawValue = parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        updateBotSetting('buyBaseOrderAmount', rawValue);
                                    }}
                                    onBlur={(e) => {
                                        const rawValue = parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        const clampedValue = Math.max(BASE_ORDER_AMOUNT, Math.min(rawValue, MAX_ORDER_AMOUNT));
                                        updateBotSetting('buyBaseOrderAmount', clampedValue);
                                    }}
                                    className={`${inputStyle} pr-12`}
                                    placeholder={BASE_ORDER_AMOUNT.toLocaleString()}
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() => handleIncrement(
                                            botSetting.buyBaseOrderAmount,
                                            'buyBaseOrderAmount',
                                            MAX_ORDER_AMOUNT
                                        )}
                                        className={spinButtonStyle}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() => handleDecrement(
                                            botSetting.buyBaseOrderAmount,
                                            'buyBaseOrderAmount',
                                            BASE_ORDER_AMOUNT
                                        )}
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
                                        const rawValue = parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        updateBotSetting('buyMaxOrderAmount', rawValue);
                                    }}
                                    onBlur={(e) => {
                                        const rawValue = parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        const clampedValue = Math.max(BASE_ORDER_AMOUNT, Math.min(rawValue, MAX_ORDER_AMOUNT));
                                        updateBotSetting('buyMaxOrderAmount', clampedValue);
                                    }}
                                    className={`${inputStyle} pr-12`}
                                    placeholder={MAX_ORDER_AMOUNT.toLocaleString()}
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() => handleIncrement(
                                            botSetting.buyMaxOrderAmount,
                                            'buyMaxOrderAmount',
                                            MAX_ORDER_AMOUNT
                                        )}
                                        className={spinButtonStyle}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() => handleDecrement(
                                            botSetting.buyMaxOrderAmount,
                                            'buyMaxOrderAmount',
                                            BASE_ORDER_AMOUNT
                                        )}
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
                                        const rawValue = parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        updateBotSetting('sellBaseOrderAmount', rawValue);
                                    }}
                                    onBlur={(e) => {
                                        const rawValue = parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        const clampedValue = Math.max(BASE_ORDER_AMOUNT, Math.min(rawValue, MAX_ORDER_AMOUNT));
                                        updateBotSetting('sellBaseOrderAmount', clampedValue);
                                    }}
                                    className={`${inputStyle} pr-12`}
                                    placeholder={BASE_ORDER_AMOUNT.toLocaleString()}
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() => handleIncrement(
                                            botSetting.sellBaseOrderAmount,
                                            'sellBaseOrderAmount',
                                            MAX_ORDER_AMOUNT
                                        )}
                                        className={spinButtonStyle}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() => handleDecrement(
                                            botSetting.sellBaseOrderAmount,
                                            'sellBaseOrderAmount',
                                            BASE_ORDER_AMOUNT
                                        )}
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
                                        const rawValue = parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        updateBotSetting('sellMaxOrderAmount', rawValue);
                                    }}
                                    onBlur={(e) => {
                                        const rawValue = parseInt(e.target.value.replace(/,/g, ''), 10) || 0;
                                        const clampedValue = Math.max(BASE_ORDER_AMOUNT, Math.min(rawValue, MAX_ORDER_AMOUNT));
                                        updateBotSetting('sellMaxOrderAmount', clampedValue);
                                    }}
                                    className={`${inputStyle} pr-12`}
                                    placeholder={MAX_ORDER_AMOUNT.toLocaleString()}
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() => handleIncrement(
                                            botSetting.sellMaxOrderAmount,
                                            'sellMaxOrderAmount',
                                            MAX_ORDER_AMOUNT
                                        )}
                                        className={spinButtonStyle}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() => handleDecrement(
                                            botSetting.sellMaxOrderAmount,
                                            'sellMaxOrderAmount',
                                            BASE_ORDER_AMOUNT
                                        )}
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

            {/* 손실/이익/시간초과 관리 설정 */}
            <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 sm:p-6 rounded-lg shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                <h3 className="text-lg font-medium text-navy-900 dark:text-white mb-4">
                    손실/이익/시간초과 관리 설정
                </h3>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* 손실 관리 영역 */}
                    <div className="space-y-4">
                        <h4 className="text-md font-medium text-navy-800 dark:text-navy-200 border-b border-navy-200 dark:border-navy-600 pb-2">
                            손실 관리
                        </h4>
                        
                        {/* 손실 관리 활성화 토글 */}
                        <div className="flex items-center justify-between p-3 bg-navy-50/50 dark:bg-navy-800/50 rounded-lg">
                            <div>
                                <h5 className="text-sm font-medium text-navy-800 dark:text-navy-200">
                                    손실 관리 전략
                                </h5>
                                <p className="text-xs text-navy-600 dark:text-navy-400">
                                    손실 임계값 도달 시 자동 매도
                                </p>
                            </div>
                            <button
                                onClick={() => updateBotSetting('isLossManagementActive', !botSetting.isLossManagementActive)}
                                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-navy-500 ${
                                    botSetting.isLossManagementActive
                                        ? 'bg-navy-500'
                                        : 'bg-gray-300 dark:bg-gray-600'
                                }`}
                            >
                                <span
                                    className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200 ${
                                        botSetting.isLossManagementActive ? 'translate-x-6' : 'translate-x-1'
                                    }`}
                                />
                            </button>
                        </div>

                        {/* 손실 임계값 */}
                        <div className={`${!botSetting.isLossManagementActive ? 'opacity-50' : ''}`}>
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
                                        updateBotSetting('lossThreshold', value);
                                    }}
                                    disabled={!botSetting.isLossManagementActive}
                                    className={`${inputStyle} pr-12 disabled:bg-gray-100 dark:disabled:bg-gray-800 disabled:cursor-not-allowed`}
                                    placeholder="1.0"
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() => {
                                            const newValue = Math.min(botSetting.lossThreshold + 0.001, 1);
                                            updateBotSetting('lossThreshold', newValue);
                                        }}
                                        disabled={!botSetting.isLossManagementActive}
                                        className={`${spinButtonStyle} disabled:opacity-50 disabled:cursor-not-allowed`}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() => {
                                            const newValue = Math.max(botSetting.lossThreshold - 0.001, 0);
                                            updateBotSetting('lossThreshold', newValue);
                                        }}
                                        disabled={!botSetting.isLossManagementActive}
                                        className={`${spinButtonStyle} disabled:opacity-50 disabled:cursor-not-allowed`}
                                    >
                                        <FaChevronDown size={8} />
                                    </button>
                                </div>
                            </div>
                            <p className="text-xs text-navy-500 dark:text-navy-400 mt-1">
                                이 비율 이상 손실 시 자동 매도 실행
                            </p>
                        </div>

                        {/* 손실 매도 비율 */}
                        <div className={`${!botSetting.isLossManagementActive ? 'opacity-50' : ''}`}>
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
                                        updateBotSetting('lossSellRatio', value);
                                    }}
                                    disabled={!botSetting.isLossManagementActive}
                                    className={`${inputStyle} pr-12 disabled:bg-gray-100 dark:disabled:bg-gray-800 disabled:cursor-not-allowed`}
                                    placeholder="90"
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() => {
                                            const newValue = Math.min(botSetting.lossSellRatio + 0.01, 1);
                                            updateBotSetting('lossSellRatio', newValue);
                                        }}
                                        disabled={!botSetting.isLossManagementActive}
                                        className={`${spinButtonStyle} disabled:opacity-50 disabled:cursor-not-allowed`}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() => {
                                            const newValue = Math.max(botSetting.lossSellRatio - 0.01, 0);
                                            updateBotSetting('lossSellRatio', newValue);
                                        }}
                                        disabled={!botSetting.isLossManagementActive}
                                        className={`${spinButtonStyle} disabled:opacity-50 disabled:cursor-not-allowed`}
                                    >
                                        <FaChevronDown size={8} />
                                    </button>
                                </div>
                            </div>
                            <p className="text-xs text-navy-500 dark:text-navy-400 mt-1">
                                손실 시 보유량의 몇 %를 매도할지 설정
                            </p>
                        </div>
                    </div>

                    {/* 이익 관리 영역 */}
                    <div className="space-y-4">
                        <h4 className="text-md font-medium text-navy-800 dark:text-navy-200 border-b border-navy-200 dark:border-navy-600 pb-2">
                            이익 관리
                        </h4>
                        
                        {/* 이익 관리 활성화 토글 */}
                        <div className="flex items-center justify-between p-3 bg-navy-50/50 dark:bg-navy-800/50 rounded-lg">
                            <div>
                                <h5 className="text-sm font-medium text-navy-800 dark:text-navy-200">
                                    이익 실현 전략
                                </h5>
                                <p className="text-xs text-navy-600 dark:text-navy-400">
                                    이익 임계값 도달 시 자동 매도
                                </p>
                            </div>
                            <button
                                onClick={() => updateBotSetting('isProfitTakingActive', !botSetting.isProfitTakingActive)}
                                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-navy-500 ${
                                    botSetting.isProfitTakingActive
                                        ? 'bg-navy-500'
                                        : 'bg-gray-300 dark:bg-gray-600'
                                }`}
                            >
                                <span
                                    className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200 ${
                                        botSetting.isProfitTakingActive ? 'translate-x-6' : 'translate-x-1'
                                    }`}
                                />
                            </button>
                        </div>

                        {/* 이익 임계값 */}
                        <div className={`${!botSetting.isProfitTakingActive ? 'opacity-50' : ''}`}>
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
                                        updateBotSetting('profitThreshold', value);
                                    }}
                                    disabled={!botSetting.isProfitTakingActive}
                                    className={`${inputStyle} pr-12 disabled:bg-gray-100 dark:disabled:bg-gray-800 disabled:cursor-not-allowed`}
                                    placeholder="1.8"
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() => {
                                            const newValue = Math.min(botSetting.profitThreshold + 0.001, 1);
                                            updateBotSetting('profitThreshold', newValue);
                                        }}
                                        disabled={!botSetting.isProfitTakingActive}
                                        className={`${spinButtonStyle} disabled:opacity-50 disabled:cursor-not-allowed`}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() => {
                                            const newValue = Math.max(botSetting.profitThreshold - 0.001, 0);
                                            updateBotSetting('profitThreshold', newValue);
                                        }}
                                        disabled={!botSetting.isProfitTakingActive}
                                        className={`${spinButtonStyle} disabled:opacity-50 disabled:cursor-not-allowed`}
                                    >
                                        <FaChevronDown size={8} />
                                    </button>
                                </div>
                            </div>
                            <p className="text-xs text-navy-500 dark:text-navy-400 mt-1">
                                이 비율 이상 이익 시 자동 매도 실행
                            </p>
                        </div>

                        {/* 이익 매도 비율 */}
                        <div className={`${!botSetting.isProfitTakingActive ? 'opacity-50' : ''}`}>
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
                                        updateBotSetting('profitSellRatio', value);
                                    }}
                                    disabled={!botSetting.isProfitTakingActive}
                                    className={`${inputStyle} pr-12 disabled:bg-gray-100 dark:disabled:bg-gray-800 disabled:cursor-not-allowed`}
                                    placeholder="50"
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() => {
                                            const newValue = Math.min(botSetting.profitSellRatio + 0.01, 1);
                                            updateBotSetting('profitSellRatio', newValue);
                                        }}
                                        disabled={!botSetting.isProfitTakingActive}
                                        className={`${spinButtonStyle} disabled:opacity-50 disabled:cursor-not-allowed`}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() => {
                                            const newValue = Math.max(botSetting.profitSellRatio - 0.01, 0);
                                            updateBotSetting('profitSellRatio', newValue);
                                        }}
                                        disabled={!botSetting.isProfitTakingActive}
                                        className={`${spinButtonStyle} disabled:opacity-50 disabled:cursor-not-allowed`}
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

                    {/* 시간초과 관리 영역 */}
                    <div className="space-y-4">
                        <h4 className="text-md font-medium text-navy-800 dark:text-navy-200 border-b border-navy-200 dark:border-navy-600 pb-2">
                            시간초과 관리
                        </h4>
                        
                        {/* 시간초과 관리 활성화 토글 */}
                        <div className="flex items-center justify-between p-3 bg-navy-50/50 dark:bg-navy-800/50 rounded-lg">
                            <div>
                                <h5 className="text-sm font-medium text-navy-800 dark:text-navy-200">
                                    시간초과 전략
                                </h5>
                                <p className="text-xs text-navy-600 dark:text-navy-400">
                                    설정 시간 경과 시 자동 매도
                                </p>
                            </div>
                            <button
                                onClick={() => updateBotSetting('isTimeOutSellActive', !botSetting.isTimeOutSellActive)}
                                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-navy-500 ${
                                    botSetting.isTimeOutSellActive
                                        ? 'bg-navy-500'
                                        : 'bg-gray-300 dark:bg-gray-600'
                                }`}
                            >
                                <span
                                    className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform duration-200 ${
                                        botSetting.isTimeOutSellActive ? 'translate-x-6' : 'translate-x-1'
                                    }`}
                                />
                            </button>
                        </div>

                        {/* 시간초과 매도 이익 비율 */}
                        <div className={`${!botSetting.isTimeOutSellActive ? 'opacity-50' : ''}`}>
                            <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                                시간초과 매도 이익 비율 (%)
                            </label>
                            <div className="relative">
                                <input
                                    type="number"
                                    step="0.1"
                                    min="0"
                                    max="100"
                                    value={(botSetting.timeOutSellProfitRatio * 100).toFixed(1)}
                                    onChange={(e) => {
                                        const value = parseFloat(e.target.value) / 100 || 0;
                                        updateBotSetting('timeOutSellProfitRatio', value);
                                    }}
                                    disabled={!botSetting.isTimeOutSellActive}
                                    className={`${inputStyle} pr-12 disabled:bg-gray-100 dark:disabled:bg-gray-800 disabled:cursor-not-allowed`}
                                    placeholder="0.1"
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() => {
                                            const newValue = Math.min(botSetting.timeOutSellProfitRatio + 0.001, 1);
                                            updateBotSetting('timeOutSellProfitRatio', newValue);
                                        }}
                                        disabled={!botSetting.isTimeOutSellActive}
                                        className={`${spinButtonStyle} disabled:opacity-50 disabled:cursor-not-allowed`}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() => {
                                            const newValue = Math.max(botSetting.timeOutSellProfitRatio - 0.001, 0);
                                            updateBotSetting('timeOutSellProfitRatio', newValue);
                                        }}
                                        disabled={!botSetting.isTimeOutSellActive}
                                        className={`${spinButtonStyle} disabled:opacity-50 disabled:cursor-not-allowed`}
                                    >
                                        <FaChevronDown size={8} />
                                    </button>
                                </div>
                            </div>
                            <p className="text-xs text-navy-500 dark:text-navy-400 mt-1">
                                시간초과 시 최소 이익 비율 (미달 시 손실 매도)
                            </p>
                        </div>

                        {/* 시간초과 매도 시간 */}
                        <div className={`${!botSetting.isTimeOutSellActive ? 'opacity-50' : ''}`}>
                            <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                                시간초과 매도 시간 (분)
                            </label>
                            <div className="relative">
                                <input
                                    type="number"
                                    step="1"
                                    min="1"
                                    max="3000"
                                    value={botSetting.timeOutSellMinutes}
                                    onChange={(e) => {
                                        const value = parseInt(e.target.value) || 1;
                                        updateBotSetting('timeOutSellMinutes', value);
                                    }}
                                    onBlur={(e) => {
                                        const value = parseInt(e.target.value) || 1;
                                        const clampedValue = Math.max(1, Math.min(value, 3000));
                                        updateBotSetting('timeOutSellMinutes', clampedValue);
                                    }}
                                    disabled={!botSetting.isTimeOutSellActive}
                                    className={`${inputStyle} pr-12 disabled:bg-gray-100 dark:disabled:bg-gray-800 disabled:cursor-not-allowed`}
                                    placeholder="45"
                                />
                                <div className="absolute right-1 top-1/2 transform -translate-y-1/2 flex flex-col gap-0.5">
                                    <button
                                        onClick={() => handleIncrement(
                                            botSetting.timeOutSellMinutes,
                                            'timeOutSellMinutes',
                                            3000
                                        )}
                                        disabled={!botSetting.isTimeOutSellActive}
                                        className={`${spinButtonStyle} disabled:opacity-50 disabled:cursor-not-allowed`}
                                    >
                                        <FaChevronUp size={8} />
                                    </button>
                                    <button
                                        onClick={() => handleDecrement(
                                            botSetting.timeOutSellMinutes,
                                            'timeOutSellMinutes',
                                            1
                                        )}
                                        disabled={!botSetting.isTimeOutSellActive}
                                        className={`${spinButtonStyle} disabled:opacity-50 disabled:cursor-not-allowed`}
                                    >
                                        <FaChevronDown size={8} />
                                    </button>
                                </div>
                            </div>
                            <p className="text-xs text-navy-500 dark:text-navy-400 mt-1">
                                매수 후 설정 시간 경과 시 자동 매도 (1-3000분)
                            </p>
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
