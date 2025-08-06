import { useEffect, useState, useMemo } from 'react';
import { FaFileExport, FaChartLine, FaHistory, FaChartArea, FaFilter, FaTimes, FaSort, FaSortUp, FaSortDown } from 'react-icons/fa';
import { formatNumber } from '../utils/format';
import { TradeResponse } from '@/api/types/trade';
import { TradingViewWidget, TechnicalAnalysis } from './index';
import { tradeApi } from '@/api/services/tradeApi';

// 필터 타입 정의
interface FilterState {
    market: string;
    type: string;
}

// 정렬 타입 정의
type SortField = 'updatedAt' | 'market' | 'type' | 'amount' | 'price' | 'totalPrice';
type SortDirection = 'asc' | 'desc' | null;

interface SortState {
    field: SortField | null;
    direction: SortDirection;
}

// CSV 내보내기 함수
const exportToCSV = (data: TradeResponse[], filename: string = 'trade_history.csv') => {
    // CSV 헤더 정의
    const headers = [
        '시간',
        '코인',
        '종류',
        '수량',
        '단가',
        '주문금액'
    ];

    // 데이터를 CSV 형식으로 변환
    const csvData = data.map(trade => [
        new Date(trade.updatedAt).toLocaleString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        }),
        trade.market,
        trade.type,
        trade.amount,
        trade.price,
        trade.totalPrice
    ]);

    // CSV 문자열 생성 (BOM 추가로 한글 깨짐 방지)
    const csvContent = [
        '\uFEFF', // BOM (Byte Order Mark) - 한글 깨짐 방지
        headers.join(','),
        ...csvData.map(row => row.join(','))
    ].join('\n');

    // Blob 생성 및 다운로드
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    
    if (link.download !== undefined) {
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
};

export default function History() {
    const [tradeHistoryData, setTradeHistoryData] = useState<TradeResponse[]>([]);
    const [activeView, setActiveView] = useState<'history' | 'chart' | 'technical'>('history');
    const [showFilters, setShowFilters] = useState(false);
    const [filters, setFilters] = useState<FilterState>({
        market: '',
        type: ''
    });
    const [sortState, setSortState] = useState<SortState>({
        field: null,
        direction: null
    });

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

    // 정렬 함수
    const sortData = (data: TradeResponse[], field: SortField, direction: SortDirection) => {
        if (!direction) return data;
        
        return [...data].sort((a, b) => {
            let aValue: string | number;
            let bValue: string | number;
            
            switch (field) {
                case 'updatedAt':
                    aValue = new Date(a.updatedAt).getTime();
                    bValue = new Date(b.updatedAt).getTime();
                    break;
                case 'market':
                    aValue = a.market;
                    bValue = b.market;
                    break;
                case 'type':
                    aValue = a.type;
                    bValue = b.type;
                    break;
                case 'amount':
                    aValue = a.amount;
                    bValue = b.amount;
                    break;
                case 'price':
                    aValue = a.price;
                    bValue = b.price;
                    break;
                case 'totalPrice':
                    aValue = a.totalPrice;
                    bValue = b.totalPrice;
                    break;

                default:
                    return 0;
            }
            
            if (aValue < bValue) return direction === 'asc' ? -1 : 1;
            if (aValue > bValue) return direction === 'asc' ? 1 : -1;
            return 0;
        });
    };

    // 필터링된 데이터 계산
    const filteredData = useMemo(() => {
        let data = tradeHistoryData.filter(trade => {
            // 코인 필터
            if (filters.market && trade.market !== filters.market) return false;
            
            // 종류 필터
            if (filters.type && trade.type !== filters.type) return false;
            

            
            return true;
        });
        
        // 정렬 적용
        if (sortState.field && sortState.direction) {
            data = sortData(data, sortState.field, sortState.direction);
        }
        
        return data;
    }, [tradeHistoryData, filters, sortState]);

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

    // 필터 초기화
    const clearFilters = () => {
        setFilters({
            market: '',
            type: ''
        });
        // 정렬 상태도 초기화
        setSortState({
            field: null,
            direction: null
        });
    };

    // 필터 변경 핸들러
    const handleFilterChange = (key: keyof FilterState, value: string) => {
        setFilters(prev => ({
            ...prev,
            [key]: value
        }));
    };

    // 정렬 핸들러
    const handleSort = (field: SortField) => {
        setSortState(prev => {
            if (prev.field === field) {
                // 같은 필드를 클릭한 경우: asc -> desc -> null 순서로 변경
                if (prev.direction === 'asc') {
                    return { field, direction: 'desc' };
                } else if (prev.direction === 'desc') {
                    return { field: null, direction: null };
                }
            }
            // 새로운 필드를 클릭한 경우: asc로 시작
            return { field, direction: 'asc' };
        });
    };

    // 정렬 아이콘 렌더링 함수
    const renderSortIcon = (field: SortField) => {
        if (sortState.field !== field) {
            return <FaSort className="w-3 h-3 text-navy-400" />;
        }
        
        if (sortState.direction === 'asc') {
            return <FaSortUp className="w-3 h-3 text-navy-600 dark:text-navy-300" />;
        }
        
        if (sortState.direction === 'desc') {
            return <FaSortDown className="w-3 h-3 text-navy-600 dark:text-navy-300" />;
        }
        
        return <FaSort className="w-3 h-3 text-navy-400" />;
    };

    // CSV 내보내기 핸들러
    const handleExportCSV = () => {
        if (filteredData.length === 0) {
            alert('내보낼 거래 내역이 없습니다.');
            return;
        }

        // 현재 날짜를 파일명에 포함
        const now = new Date();
        const dateStr = now.toISOString().split('T')[0]; // YYYY-MM-DD 형식
        const filename = `trade_history_${dateStr}.csv`;
        
        exportToCSV(filteredData, filename);
    };

    // 활성 필터 개수 계산
    const activeFilterCount = Object.values(filters).filter(value => value !== '').length;

    // 거래 내역이 없는지 확인하는 함수
    const hasNoTradeHistory = filteredData.length === 0;

    // 고유한 값들 추출 (필터 옵션용)
    const uniqueMarkets = Array.from(new Set(tradeHistoryData.map(trade => trade.market)));
    const uniqueTypes = Array.from(new Set(tradeHistoryData.map(trade => trade.type)));

    // 매수 총액 계산
    const totalBuyAmount = useMemo(() => {
        return filteredData.reduce((total, trade) => {
            if (trade.type === '매수') {
                return total + trade.totalPrice;
            }
            return total;
        }, 0);
    }, [filteredData]);

    // 매도 총액 계산
    const totalSellAmount = useMemo(() => {
        return filteredData.reduce((total, trade) => {
            if (trade.type === '매도') {
                return total + trade.totalPrice;
            }
            return total;
        }, 0);
    }, [filteredData]);

    // 총 수익 계산 (매도 총액 - 매수 총액)
    const totalProfit = totalSellAmount - totalBuyAmount;

    // 수익률 계산 (매수 총액 대비)
    const profitRate = totalBuyAmount > 0 ? (totalProfit / totalBuyAmount) * 100 : 0;

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
                    {/* 필터 섹션 */}
                    <div className="mb-6">
                        <div className="flex justify-between items-center mb-4">
                            <div className="flex items-center space-x-3">
                                <button
                                    onClick={() => setShowFilters(!showFilters)}
                                    className={`flex items-center space-x-2 px-4 py-2 rounded-md text-sm font-medium transition-all duration-200 ${
                                        showFilters 
                                            ? 'bg-navy-500 text-white shadow-lg shadow-navy-500/30' 
                                            : 'bg-navy-100 dark:bg-navy-700 text-navy-700 dark:text-navy-300 hover:bg-navy-200 dark:hover:bg-navy-600'
                                    }`}
                                >
                                    <FaFilter className="w-4 h-4" />
                                    <span>필터</span>
                                    {activeFilterCount > 0 && (
                                        <span className="bg-navy-600 dark:bg-navy-400 text-white dark:text-navy-900 text-xs rounded-full px-2 py-0.5 min-w-[20px] font-medium">
                                            {activeFilterCount}
                                        </span>
                                    )}
                                </button>
                                {activeFilterCount > 0 && (
                                    <button
                                        onClick={clearFilters}
                                        className="flex items-center space-x-1 px-3 py-2 text-xs text-navy-600 hover:text-navy-700 dark:text-navy-400 dark:hover:text-navy-300 bg-navy-50 dark:bg-navy-800 rounded-md hover:bg-navy-100 dark:hover:bg-navy-700 transition-colors duration-200"
                                    >
                                        <FaTimes className="w-3 h-3" />
                                        <span>초기화</span>
                                    </button>
                                )}
                            </div>
                            <div className="flex items-center space-x-4">
                                {sortState.field && sortState.direction && (
                                    <div className="flex items-center space-x-1 text-xs text-navy-600 dark:text-navy-400">
                                        <span>정렬:</span>
                                        <span className="font-medium">
                                            {sortState.field === 'updatedAt' && '시간'}
                                            {sortState.field === 'market' && '코인'}
                                            {sortState.field === 'type' && '종류'}
                                            {sortState.field === 'amount' && '수량'}
                                            {sortState.field === 'price' && '단가'}
                                            {sortState.field === 'totalPrice' && '주문금액'}
                                        </span>
                                        <span className="text-navy-500">
                                            ({sortState.direction === 'asc' ? '오름차순' : '내림차순'})
                                        </span>
                                    </div>
                                )}
                                <div className="text-sm text-navy-600 dark:text-navy-400 font-medium">
                                    총 {filteredData.length}건의 거래 내역
                                </div>
                            </div>
                        </div>

                        {/* 필터 패널 */}
                        {showFilters && (
                            <div className="bg-navy-50 dark:bg-navy-800 rounded-md p-4 mb-4">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    {/* 코인 필터 */}
                                    <div>
                                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                                            코인
                                        </label>
                                        <select
                                            value={filters.market}
                                            onChange={(e) => handleFilterChange('market', e.target.value)}
                                            className="w-full px-3 py-2 border border-navy-300 dark:border-navy-600 rounded-md bg-white dark:bg-navy-700 text-navy-900 dark:text-white text-sm focus:ring-2 focus:ring-navy-500 focus:border-transparent"
                                        >
                                            <option value="">전체</option>
                                            {uniqueMarkets.map(market => (
                                                <option key={market} value={market}>{market}</option>
                                            ))}
                                        </select>
                                    </div>

                                    {/* 종류 필터 */}
                                    <div>
                                        <label className="block text-sm font-medium text-navy-700 dark:text-navy-300 mb-2">
                                            종류
                                        </label>
                                        <select
                                            value={filters.type}
                                            onChange={(e) => handleFilterChange('type', e.target.value)}
                                            className="w-full px-3 py-2 border border-navy-300 dark:border-navy-600 rounded-md bg-white dark:bg-navy-700 text-navy-900 dark:text-white text-sm focus:ring-2 focus:ring-navy-500 focus:border-transparent"
                                        >
                                            <option value="">전체</option>
                                            {uniqueTypes.map(type => (
                                                <option key={type} value={type}>{type}</option>
                                            ))}
                                        </select>
                                    </div>


                                </div>
                            </div>
                        )}
                    </div>

                    {/* 거래 내역이 없을 때 표시할 메시지 */}
                    {hasNoTradeHistory ? (
                        <div className="flex flex-col items-center justify-center py-12 text-center">
                            <div className="text-navy-400 dark:text-navy-500 mb-4">
                                <FaHistory size={48} />
                            </div>
                            <h3 className="text-lg font-medium text-navy-700 dark:text-navy-300 mb-2">
                                {tradeHistoryData.length === 0 ? '아직 거래 내역이 없습니다' : '필터 조건에 맞는 거래 내역이 없습니다'}
                            </h3>
                            <p className="text-sm text-navy-500 dark:text-navy-400">
                                {tradeHistoryData.length === 0 
                                    ? '거래를 시작하면 여기에 거래 내역이 표시됩니다'
                                    : '다른 필터 조건을 시도해보세요'
                                }
                            </p>
                        </div>
                    ) : (
                        <>
                            {/* 모바일 뷰 */}
                            <div className="grid grid-cols-1 gap-4 sm:hidden">
                                {filteredData.map((trade, index) => (
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

                                        </div>
                                    </div>
                                ))}
                            </div>

                            {/* 데스크톱 뷰 */}
                            <div className="hidden sm:block overflow-x-auto">
                                <table className="min-w-full divide-y divide-navy-200 dark:divide-navy-700">
                                    <thead>
                                        <tr>
                                            <th 
                                                className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider cursor-pointer hover:bg-navy-50 dark:hover:bg-navy-700/50 transition-colors duration-150"
                                                onClick={() => handleSort('updatedAt')}
                                            >
                                                <div className="flex items-center space-x-1">
                                                    <span>시간</span>
                                                    {renderSortIcon('updatedAt')}
                                                </div>
                                            </th>
                                            <th 
                                                className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider cursor-pointer hover:bg-navy-50 dark:hover:bg-navy-700/50 transition-colors duration-150"
                                                onClick={() => handleSort('market')}
                                            >
                                                <div className="flex items-center space-x-1">
                                                    <span>코인</span>
                                                    {renderSortIcon('market')}
                                                </div>
                                            </th>
                                            <th 
                                                className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider cursor-pointer hover:bg-navy-50 dark:hover:bg-navy-700/50 transition-colors duration-150"
                                                onClick={() => handleSort('type')}
                                            >
                                                <div className="flex items-center space-x-1">
                                                    <span>종류</span>
                                                    {renderSortIcon('type')}
                                                </div>
                                            </th>
                                            <th 
                                                className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider cursor-pointer hover:bg-navy-50 dark:hover:bg-navy-700/50 transition-colors duration-150"
                                                onClick={() => handleSort('amount')}
                                            >
                                                <div className="flex items-center space-x-1">
                                                    <span>수량</span>
                                                    {renderSortIcon('amount')}
                                                </div>
                                            </th>
                                            <th 
                                                className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider cursor-pointer hover:bg-navy-50 dark:hover:bg-navy-700/50 transition-colors duration-150"
                                                onClick={() => handleSort('price')}
                                            >
                                                <div className="flex items-center space-x-1">
                                                    <span>단가</span>
                                                    {renderSortIcon('price')}
                                                </div>
                                            </th>
                                            <th 
                                                className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-400 uppercase tracking-wider cursor-pointer hover:bg-navy-50 dark:hover:bg-navy-700/50 transition-colors duration-150"
                                                onClick={() => handleSort('totalPrice')}
                                            >
                                                <div className="flex items-center space-x-1">
                                                    <span>주문금액</span>
                                                    {renderSortIcon('totalPrice')}
                                                </div>
                                            </th>

                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-navy-200 dark:divide-navy-700">
                                        {filteredData.map((trade) => (
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

                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </>
                    )}

                    {/* 총 수익 정보 및 CSV 내보내기 */}
                    {!hasNoTradeHistory && (
                        <div className="mt-6">
                            {/* 총 수익 정보 */}
                            <div className="bg-navy-50 dark:bg-navy-800 rounded-md p-4 mb-4">
                                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                                    <div className="text-center">
                                        <p className="text-sm text-navy-600 dark:text-navy-400 mb-1">매수 총액</p>
                                        <p className="text-lg font-bold font-kimm text-navy-700 dark:text-navy-300">
                                            {formatNumber(totalBuyAmount)}원
                                        </p>
                                    </div>
                                    <div className="text-center">
                                        <p className="text-sm text-navy-600 dark:text-navy-400 mb-1">매도 총액</p>
                                        <p className="text-lg font-bold font-kimm text-navy-700 dark:text-navy-300">
                                            {formatNumber(totalSellAmount)}원
                                        </p>
                                    </div>
                                    <div className="text-center">
                                        <p className="text-sm text-navy-600 dark:text-navy-400 mb-1">총 수익</p>
                                        <p className={`text-lg font-bold font-kimm ${
                                            totalProfit > 0 
                                                ? 'text-green-600 dark:text-green-400' 
                                                : totalProfit < 0 
                                                ? 'text-red-600 dark:text-red-400' 
                                                : 'text-navy-700 dark:text-navy-300'
                                        }`}>
                                            {totalProfit > 0 ? '+' : ''}{formatNumber(totalProfit)}원
                                        </p>
                                    </div>
                                    <div className="text-center">
                                        <p className="text-sm text-navy-600 dark:text-navy-400 mb-1">수익률</p>
                                        <p className={`text-lg font-bold font-kimm ${
                                            profitRate > 0 
                                                ? 'text-green-600 dark:text-green-400' 
                                                : profitRate < 0 
                                                ? 'text-red-600 dark:text-red-400' 
                                                : 'text-navy-700 dark:text-navy-300'
                                        }`}>
                                            {profitRate > 0 ? '+' : ''}{profitRate.toFixed(2)}%
                                        </p>
                                    </div>
                                </div>
                            </div>

                            {/* CSV 내보내기 버튼 */}
                            <div className="flex flex-row justify-end items-center">
                                <button 
                                    onClick={handleExportCSV}
                                    className="flex items-center justify-center space-x-2 px-4 py-2 bg-navy-500 hover:bg-navy-600 text-white text-[0.7rem] rounded-md transition-colors duration-200 shadow-lg shadow-navy-500/30"
                                >
                                    <FaFileExport />
                                    <span>CSV 내보내기</span>
                                </button>
                            </div>
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
