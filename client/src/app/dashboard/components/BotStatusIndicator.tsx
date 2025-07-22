import { useEffect } from 'react';
import { useBotStore } from '@/store/botStore';

export default function BotStatusIndicator() {
    const { isActive, isLoading, fetchBotStatus, toggleBot } = useBotStore();

    useEffect(() => {
        fetchBotStatus();
    }, [fetchBotStatus]);

    return (
        <div 
            className="flex items-center space-x-4 bg-white/10 px-4 py-2 rounded-full cursor-pointer hover:bg-white/20 transition-colors duration-200"
            onClick={toggleBot}
        >
            <span
                className={`inline-block w-3 h-3 rounded-full ${isActive ? 'bg-emerald-500 animate-pulse' : 'bg-red-500'}`}
            ></span>
            <span className="text-white/90 text-sm font-medium">
                {isActive ? '실행 중' : '중지됨'}
            </span>
        </div>
    );
}
