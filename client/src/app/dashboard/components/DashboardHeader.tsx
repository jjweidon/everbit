import BotStatusIndicator from './BotStatusIndicator';

interface DashboardHeaderProps {
  botStatus: boolean;
}

export default function DashboardHeader({ botStatus }: DashboardHeaderProps) {
  return (
    <div className="bg-gradient-to-r from-navy-600 to-navy-700">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-bold text-white font-kimm">Dashboard</h1>
          <BotStatusIndicator isRunning={botStatus} />
        </div>
      </div>
    </div>
  );
} 