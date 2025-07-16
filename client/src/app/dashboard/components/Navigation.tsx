import { FaChartLine, FaChartBar, FaHistory, FaCog } from 'react-icons/fa';

interface NavigationProps {
  selectedTab: string;
  setSelectedTab: (tab: string) => void;
}

export default function Navigation({ selectedTab, setSelectedTab }: NavigationProps) {
  return (
    <div className="bg-white/50 dark:bg-navy-800/50 backdrop-blur-xl border-b border-navy-200 dark:border-navy-700 sticky top-0 z-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <nav className="flex space-x-8">
          <button
            onClick={() => setSelectedTab('overview')}
            className={`px-4 py-4 text-sm font-medium border-b-2 transition-all duration-200 flex items-center ${
              selectedTab === 'overview'
                ? 'border-navy-500 text-navy-600 dark:text-navy-300'
                : 'border-transparent text-navy-500 hover:text-navy-700 hover:border-navy-300 dark:text-navy-400 dark:hover:text-white'
            }`}
          >
            <FaChartLine className="inline-block mr-2" />
            Overview
          </button>
          <button
            onClick={() => setSelectedTab('portfolio')}
            className={`px-4 py-4 text-sm font-medium border-b-2 transition-all duration-200 flex items-center ${
              selectedTab === 'portfolio'
                ? 'border-navy-500 text-navy-600 dark:text-navy-300'
                : 'border-transparent text-navy-500 hover:text-navy-700 hover:border-navy-300 dark:text-navy-400 dark:hover:text-white'
            }`}
          >
            <FaChartBar className="inline-block mr-2" />
            Portfolio
          </button>
          <button
            onClick={() => setSelectedTab('history')}
            className={`px-4 py-4 text-sm font-medium border-b-2 transition-all duration-200 flex items-center ${
              selectedTab === 'history'
                ? 'border-navy-500 text-navy-600 dark:text-navy-300'
                : 'border-transparent text-navy-500 hover:text-navy-700 hover:border-navy-300 dark:text-navy-400 dark:hover:text-white'
            }`}
          >
            <FaHistory className="inline-block mr-2" />
            History
          </button>
          <button
            onClick={() => setSelectedTab('settings')}
            className={`px-4 py-4 text-sm font-medium border-b-2 transition-all duration-200 flex items-center ${
              selectedTab === 'settings'
                ? 'border-navy-500 text-navy-600 dark:text-navy-300'
                : 'border-transparent text-navy-500 hover:text-navy-700 hover:border-navy-300 dark:text-navy-400 dark:hover:text-white'
            }`}
          >
            <FaCog className="inline-block mr-2" />
            Settings
          </button>
        </nav>
      </div>
    </div>
  );
} 