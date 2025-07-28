import { useState, useEffect } from 'react';

export function useDarkMode() {
    const [isDarkMode, setIsDarkMode] = useState(false);

    useEffect(() => {
        // 시스템의 다크모드 상태를 감지하는 함수
        const checkDarkMode = () => {
            const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            setIsDarkMode(isDark);
        };

        // 초기 상태 설정
        checkDarkMode();

        // 다크모드 변경 이벤트 리스너 등록
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
        const handleChange = (e: MediaQueryListEvent) => {
            setIsDarkMode(e.matches);
        };

        // 이벤트 리스너 추가
        mediaQuery.addEventListener('change', handleChange);

        // 클린업 함수
        return () => {
            mediaQuery.removeEventListener('change', handleChange);
        };
    }, []);

    return isDarkMode;
} 