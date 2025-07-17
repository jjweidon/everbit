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
    <div className="bg-gradient-to-r from-navy-600 to-navy-700">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-bold text-white font-kimm">{title}</h1>
          <div className="flex items-center space-x-4">
            <BotStatusIndicator isRunning={botStatus} />
            <button
              onClick={handleMypageClick}
              className="p-2 rounded-full hover:bg-navy-500 transition-colors"
            >
              <FaUser className="h-5 w-5 text-white" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
} 