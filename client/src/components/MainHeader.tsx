import Image from 'next/image';
import BotStatusIndicator from '@/app/dashboard/components/BotStatusIndicator';
import { FaRegQuestionCircle, FaUser } from 'react-icons/fa';
import Link from 'next/link';

interface MainHeaderProps {
    title: string;
    showControls?: boolean;
}

export default function MainHeader({
    title,
    showControls = true,
}: MainHeaderProps) {
    return (
        <div className="bg-gradient-to-br from-navy-800 to-navy-700 backdrop-blur-lg bg-opacity-80">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3">
                <div className="flex justify-between items-center">
                    <Link href="/dashboard" className="flex items-center gap-2 cursor-pointer">
                        <Image
                            src="/logos/logo-icon-2d.webp"
                            alt="Everbit Logo"
                            width={24}
                            height={24}
                            className="w-6 h-6"
                        />
                        <h1 className="text-lg sm:text-xl font-medium text-white font-kimm tracking-wide">
                            {title}
                        </h1>
                    </Link>
                    {showControls && (
                        <div className="flex items-center space-x-3">
                            <Link
                                href="/docs"
                                className="cursor-pointer hover:bg-navy-500/50 rounded-lg p-1.5 transition-colors duration-200"
                            >
                                <FaRegQuestionCircle className="h-4 w-4 text-white/90" />
                            </Link>
                            <BotStatusIndicator />
                            <Link
                                href="/mypage"
                                className="cursor-pointer hover:bg-navy-500/50 rounded-lg p-1.5 transition-colors duration-200"
                            >
                                <FaUser className="h-4 w-4 text-white/90" />
                            </Link>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
