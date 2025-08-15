// TradingViewWidget.jsx
import React, { useEffect, useRef, memo, useState } from 'react';
import { useDarkMode } from '@/hooks/useDarkMode';
import { tradeApi } from '@/api/services/tradeApi';
import { MarketResponse } from '@/api/types/trade';

interface TradingViewWidgetProps {
    symbol?: string;
    interval?: string;
    theme?: 'light' | 'dark';
    locale?: string;
    timezone?: string;
    allowSymbolChange?: boolean;
    hideSideToolbar?: boolean;
    hideTopToolbar?: boolean;
    hideLegend?: boolean;
    hideVolume?: boolean;
    backgroundColor?: string;
    gridColor?: string;
    style?: string;
    width?: string;
    height?: string;
    autosize?: boolean;
    saveImage?: boolean;
    calendar?: boolean;
    details?: boolean;
    hotlist?: boolean;
    withDateRanges?: boolean;
    watchlist?: string[];
    compareSymbols?: string[];
    studies?: string[];
}

function TradingViewWidget({
    symbol = 'UPBIT:BTCKRW',
    interval = '15',
    theme: propTheme,
    locale = 'kr',
    timezone = 'Asia/Seoul',
    allowSymbolChange = true,
    hideSideToolbar = true,
    hideTopToolbar = false,
    hideLegend = true,
    hideVolume = true,
    backgroundColor,
    gridColor,
    style = '1',
    width = '100%',
    height = '100%',
    autosize = true,
    saveImage = true,
    calendar = false,
    details = false,
    hotlist = true,
    withDateRanges = false,
    watchlist: propWatchlist,
    compareSymbols = [],
    studies = []
}: TradingViewWidgetProps) {
    const container = useRef<HTMLDivElement>(null);
    const isDarkMode = useDarkMode();
    const [markets, setMarkets] = useState<MarketResponse[]>([]);
    const [watchlist, setWatchlist] = useState<string[]>([]);

    // 마켓 데이터를 TradingView 형식으로 변환하는 함수
    const convertMarketToTradingViewSymbol = (market: string): string => {
        return `UPBIT:${market}KRW`;
    };

    // API에서 마켓 데이터를 로드하는 함수
    const loadMarkets = async () => {
        try {
            const response = await tradeApi.getMarkets();
            setMarkets(response);
            
            // 마켓 데이터를 TradingView 형식으로 변환하여 watchlist 설정
            const tradingViewSymbols = response.map(market => 
                convertMarketToTradingViewSymbol(market.market)
            );
            setWatchlist(tradingViewSymbols);
        } catch (error) {
            console.error('마켓 데이터 로드 실패:', error);
            // 실패 시 기본 watchlist 사용
            setWatchlist([
                'UPBIT:BTCKRW',
                'UPBIT:ETHKRW',
                'UPBIT:SOLKRW'
            ]);
        }
    };

    // 컴포넌트 마운트 시 마켓 데이터 로드
    useEffect(() => {
        loadMarkets();
    }, []);
    
    // props로 전달받은 watchlist가 있으면 그것을 사용하고, 없으면 API에서 로드한 watchlist 사용
    const finalWatchlist = propWatchlist || watchlist;
    
    // 다크모드 상태에 따라 테마와 배경색, 그리드색을 동적으로 설정
    const theme = propTheme || (isDarkMode ? 'dark' : 'light');
    const dynamicBackgroundColor = backgroundColor || (isDarkMode ? 'rgba(0, 0, 0, 1)' : 'rgba(255, 255, 255, 1)');
    const dynamicGridColor = gridColor || (isDarkMode ? 'rgba(242, 242, 242, 0.06)' : 'rgba(0, 0, 0, 0.06)');

    useEffect(() => {
        if (!container.current) return;

        // 기존 내용을 모두 제거
        container.current.innerHTML = '';

        // 설정 객체 생성
        const config = {
            allow_symbol_change: allowSymbolChange,
            calendar,
            details,
            hide_side_toolbar: hideSideToolbar,
            hide_top_toolbar: hideTopToolbar,
            hide_legend: hideLegend,
            hide_volume: hideVolume,
            hotlist,
            interval,
            locale,
            save_image: saveImage,
            style,
            symbol,
            theme,
            timezone,
            backgroundColor: dynamicBackgroundColor,
            gridColor: dynamicGridColor,
            watchlist: finalWatchlist,
            withdateranges: withDateRanges,
            compareSymbols,
            studies,
            autosize,
            width,
            height
        };

        // URL 인코딩된 설정 문자열 생성
        const configString = encodeURIComponent(JSON.stringify(config));
        const iframeSrc = `https://www.tradingview-widget.com/embed-widget/advanced-chart/?locale=${locale}#${configString}`;

        // TradingView 위젯을 직접 iframe으로 생성
        const iframe = document.createElement('iframe') as HTMLIFrameElement;
        iframe.src = iframeSrc;
        iframe.style.width = '100%';
        iframe.style.height = '100%';
        iframe.style.border = 'none';
        (iframe as any).allowTransparency = true;
        iframe.title = 'TradingView Advanced Chart';

        container.current.appendChild(iframe);

        // 클린업 함수
        return () => {
            if (container.current) {
                container.current.innerHTML = '';
            }
        };
    }, [
        symbol, interval, theme, locale, timezone, allowSymbolChange,
        hideSideToolbar, hideTopToolbar, hideLegend, hideVolume,
        dynamicBackgroundColor, dynamicGridColor, style, width, height, autosize,
        saveImage, calendar, details, hotlist, withDateRanges,
        finalWatchlist, compareSymbols, studies, isDarkMode
    ]);

    return (
        <div
            className="tradingview-widget-container"
            ref={container}
            style={{ height: '100%', width: '100%' }}
        >
            <div className="tradingview-widget-container__widget" style={{ height: 'calc(100% - 32px)', width: '100%' }}></div>
        </div>
    );
}

export default memo(TradingViewWidget);