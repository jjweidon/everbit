import { DashboardTab } from '../types';
import { DASHBOARD_TABS } from '../constants';

interface NavigationProps {
  selectedTab: DashboardTab;
  setSelectedTab: (tab: DashboardTab) => void;
}

export default function Navigation({ selectedTab, setSelectedTab }: NavigationProps) {
  return (
    <div className="bg-white/50 dark:bg-navy-800/50 backdrop-blur-xl border-b border-navy-200 dark:border-navy-700 sticky top-0 z-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <nav className="flex space-x-8">
          {DASHBOARD_TABS.map(({ id, label, icon: Icon }) => (
            <button
              key={id}
              onClick={() => setSelectedTab(id)}
              className={`
                px-4 py-4 text-sm font-medium border-b-2 transition-all duration-200 flex items-center
                ${selectedTab === id
                  ? 'border-navy-500 text-navy-600 dark:text-navy-300'
                  : 'border-transparent text-navy-500 hover:text-navy-700 hover:border-navy-300 dark:text-navy-400 dark:hover:text-white'
                }
              `}
            >
              <Icon className="inline-block mr-2" />
              {label}
            </button>
          ))}
        </nav>
      </div>
    </div>
  );
} 