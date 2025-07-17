import { useRouter } from 'next/navigation';
import BotStatusIndicator from '@/app/dashboard/components/BotStatusIndicator';
import { FaUser } from 'react-icons/fa';

interface MainHeaderProps {
    title: string;
    botStatus: boolean;
}

export default function MainHeader({ title, botStatus }: MainHeaderProps) {
    const router = useRouter();

    const handleMypageClick = () => {
        router.push('/mypage');
    };

    return (
        <div className="bg-gradient-to-r from-navy-600 to-navy-700 backdrop-blur-lg bg-opacity-80">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3">
                <div className="flex justify-between items-center">
                    <h1 className="text-lg sm:text-xl font-medium text-white font-kimm tracking-wide">
                        {title}
                    </h1>
                    <div className="flex items-center space-x-3">
                        <BotStatusIndicator isRunning={botStatus} />
                        <button
                            onClick={handleMypageClick}
                            className="p-1.5 rounded-full hover:bg-navy-500/50 transition-colors duration-200"
                        >
                            <FaUser className="h-4 w-4 text-white/90" />
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
