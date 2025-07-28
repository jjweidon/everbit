// TechnicalAnalysis.tsx
import React, { useEffect, useRef, memo } from 'react';

interface TechnicalAnalysisProps {
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

function TechnicalAnalysis({
    symbol = 'UPBIT:BTCKRW',
    interval = 'D',
    theme = 'dark',
    locale = 'kr',
    timezone = 'Asia/Seoul',
    allowSymbolChange = true,
    hideSideToolbar = false,
    hideTopToolbar = true,
    hideLegend = false,
    hideVolume = false,
    backgroundColor = 'rgba(0, 0, 0, 1)',
    gridColor = 'rgba(242, 242, 242, 0.06)',
    style = '1',
    width = '100%',
    height = '100%',
    autosize = true,
    saveImage = false,
    calendar = false,
    details = false,
    hotlist = true,
    withDateRanges = true,
    watchlist = [
        'UPBIT:BTCKRW',
        'UPBIT:ETHKRW',
        'UPBIT:SOLKRW',
        'UPBIT:DOGEKRW',
        'UPBIT:USDTKRW'
    ],
    compareSymbols = [],
    studies = [
        'ROC@tv-basicstudies',
        'StochasticRSI@tv-basicstudies',
        'MASimple@tv-basicstudies'
    ]
}: TechnicalAnalysisProps) {
    const container = useRef<HTMLDivElement>(null);

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
            backgroundColor,
            gridColor,
            watchlist,
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
        iframe.title = 'TradingView Technical Analysis';

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
        backgroundColor, gridColor, style, width, height, autosize,
        saveImage, calendar, details, hotlist, withDateRanges,
        watchlist, compareSymbols, studies
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

export default memo(TechnicalAnalysis);
