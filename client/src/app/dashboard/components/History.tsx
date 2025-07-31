import { useEffect, useState } from 'react';
import { FaFileExport, FaChartLine, FaHistory, FaChartArea } from 'react-icons/fa';
import { formatNumber } from '../utils/format';
import { TradeResponse } from '@/api/types/trade';
import { TradingViewWidget, TechnicalAnalysis } from './index';
import { tradeApi } from '@/api/services/tradeApi';

export default function History() {
    const [tradeHistoryData, setTradeHistoryData] = useState<TradeResponse[]>([]);
    const [activeView, setActiveView] = useState<'history' | 'chart' | 'technical'>('history');

    useEffect(() => {
        console.log('activeView', activeView);
        if (activeView === 'history') {
            getTradeHistory();
        }
    }, [activeView]);

    const getTradeHistory = async () => {
        try {
            const response = await tradeApi.getTrades();
            setTradeHistoryData(response);
        } catch (error) {
            console.error('거래 내역 조회 실패:', error);
        }
    };

    const formatDate = (date: Date) => {
        return new Date(date).toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    };

    // 거래 내역이 없는지 확인하는 함수
    const hasNoTradeHistory = tradeHistoryData.length === 0;

    return (
        <div>
            {/* 토글 버튼 */}
            <div className="flex justify-start pb-4">
                <div className="relative flex bg-navy-100 dark:bg-navy-700 rounded-md w-[360px]">
                    {/* 슬라이드 배경 */}
                    <div
                        className={`absolute top-1 bottom-1 bg-navy-500 rounded-md transition-all duration-300 ease-in-out shadow-lg shadow-navy-500/30 ${
                            activeView === 'history'
                                ? 'left-1 w-[120px]'
                                : activeView === 'chart'
                                ? 'left-[121px] w-[120px]'
                                : 'left-[241px] w-[120px]'
                        }`}
                    />

                    <button
                        onClick={() => setActiveView('history')}
                        className={`relative flex items-center justify-center space-x-2 px-4 py-2 rounded-md transition-all duration-200 font-medium z-10 w-[120px] ${
                            activeView === 'history'
                                ? 'text-white'
                                : 'text-navy-600 dark:text-navy-300 hover:text-navy-900 dark:hover:text-white'
                        }`}
                    >
                        <FaHistory />
                        <span>거래 내역</span>
                    </button>
                    <button
                        onClick={() => setActiveView('chart')}
                        className={`relative flex items-center justify-center space-x-2 px-4 py-2 rounded-md transition-all duration-200 font-medium z-10 w-[120px] ${
                            activeView === 'chart'
                                ? 'text-white'
                                : 'text-navy-600 dark:text-navy-300 hover:text-navy-900 dark:hover:text-white'
                        }`}
                    >
                        <FaChartLine />
                        <span>차트</span>
                    </button>
                    <button
                        onClick={() => setActiveView('technical')}
                        className={`relative flex items-center justify-center space-x-2 px-4 py-2 rounded-md transition-all duration-200 font-medium z-10 w-[120px] ${
                            activeView === 'technical'
                                ? 'text-white'
                                : 'text-navy-600 dark:text-navy-300 hover:text-navy-900 dark:hover:text-white'
                        }`}
                    >
                        <FaChartArea />
                        <span>분석</span>
                    </button>   
                </div>
            </div>

            {/* 거래 내역 */}
            {activeView === 'history' && (
                <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 p-4 rounded-md shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                    {/* 거래 내역이 없을 때 표시할 메시지 */}
                    {hasNoTradeHistory ? (
                        <div className="flex flex-col items-center justify-center py-12 text-center">
                            <div className="text-navy-400 dark:text-navy-500 mb-4">
                                <FaHistory size={48} />
                            </div>
                            <h3 className="text-lg font-medium text-navy-700 dark:text-navy-300 mb-2">
                                아직 거래 내역이 없습니다
                            </h3>
                            <p className="text-sm text-navy-500 dark:text-navy-400">
                                거래를 시작하면 여기에 거래 내역이 표시됩니다
                            </p>
                        </div>
                    ) : (
                        <>
                            {/* 모바일 뷰 */}
                            <div className="grid grid-cols-1 gap-4 sm:hidden">
                                {tradeHistoryData.map((trade, index) => (
                                    <div
                                        key={trade.tradeId}
                                        className="bg-navy-50/50 dark:bg-navy-800 rounded-md p-4 space-y-2"
                                    >
                                        <div className="flex justify-between items-center">
                                            <span className="text-lg font-medium text-navy-900 dark:text-white font-kimm">
                                                {trade.market}
                                            </span>
                                            <span
                                                className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                                    trade.type === '매수'
                                                        ? 'trade-badge-entry'
                                                        : 'trade-badge-exit'
                                                }`}
                                            >
                                                {trade.type}
                                            </span>
                                        </div>
                                        <div className="grid grid-cols-2 gap-2 text-sm">
                                            <div>
                                                <p className="text-navy-500 dark:text-navy-400">시간</p>
                                                <p className="text-navy-600 dark:text-navy-300 text-xs">
                                                    {formatDate(trade.updatedAt)}
                                                </p>
                                            </div>
                                            <div>
                                                <p className="text-navy-500 dark:text-navy-400">수량</p>
                                                <p className="text-navy-600 dark:text-navy-300">
                                                    {trade.amount}
                                                </p>
                                            </div>
                                            <div>
                                                <p className="text-navy-500 dark:text-navy-400">단가</p>
                                                <p className="text-navy-600 dark:text-navy-300">
                                                    {formatNumber(trade.price)}원
                                                </p>
                                            </div>
                                            <div>
                                                <p className="text-navy-500 dark:text-navy-400">주문금액</p>
                                                <p className="text-navy-600 dark:text-navy-300">
                                                    {formatNumber(trade.totalPrice)}원
                                                </p>
                                            </div>
                                            <div className="col-span-2">
                                                <p className="text-navy-500 dark:text-navy-400">상태</p>
                                                <p className="text-navy-600 dark:text-navy-300">
                                                    {trade.status}
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
                                                종류
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                                수량
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                                단가
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                                주문금액
                                            </th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider">
                                                상태
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-navy-200 dark:divide-navy-700">
                                        {tradeHistoryData.map((trade) => (
                                            <tr
                                                key={trade.tradeId}
                                                className="hover:bg-navy-50 dark:hover:bg-navy-700/50 transition-colors duration-150"
                                            >
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                                    {formatDate(trade.updatedAt)}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-navy-900 dark:text-white font-kimm">
                                                    {trade.market}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                                    <span
                                                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                                            trade.type === '매수'
                                                                ? 'trade-badge-entry'
                                                                : 'trade-badge-exit'
                                                        }`}
                                                    >
                                                        {trade.type}
                                                    </span>
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                                    {trade.amount}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                                    {formatNumber(trade.price)}원
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                                    {formatNumber(trade.totalPrice)}원
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-600 dark:text-navy-300">
                                                    {trade.status}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </>
                    )}

                    {/* CSV 내보내기 버튼은 거래 내역이 있을 때만 표시 */}
                    {!hasNoTradeHistory && (
                        <div className="flex flex-row justify-end items-center mt-4">
                            <button className="flex items-center justify-center space-x-2 px-4 py-2 bg-navy-500 hover:bg-navy-600 text-white text-[0.7rem] rounded-md transition-colors duration-200 shadow-lg shadow-navy-500/30">
                                <FaFileExport />
                                <span>CSV 내보내기</span>
                            </button>
                        </div>
                    )}
                </div>
            )}

            {/* TradingView 차트 */}
            {activeView === 'chart' && (
                <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 rounded-md shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                    <div className="h-[36rem]">
                        <TradingViewWidget />
                    </div>
                </div>
            )}

            {/* 기술적 분석 */}
            {activeView === 'technical' && (
                <div className="bg-white dark:bg-gradient-to-br dark:from-navy-800 dark:to-navy-700 rounded-md shadow-lg shadow-navy-200/50 dark:shadow-navy-900/50 border border-navy-200/50 dark:border-navy-700/50">
                    <div className="h-[36rem]">
                        <TechnicalAnalysis />
                    </div>
                </div>
            )}
        </div>
    );
}
