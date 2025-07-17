interface BotStatusIndicatorProps {
    isRunning: boolean;
}

export default function BotStatusIndicator({ isRunning }: BotStatusIndicatorProps) {
    return (
        <div className="flex items-center space-x-4 bg-white/10 px-4 py-2 rounded-full">
            <span
                className={`inline-block w-3 h-3 rounded-full ${isRunning ? 'bg-emerald-500 animate-pulse' : 'bg-red-500'}`}
            ></span>
            <span className="text-white/90 text-sm font-medium">
                {isRunning ? '실행 중' : '중지됨'}
            </span>
        </div>
    );
}
