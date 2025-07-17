import { DashboardTab } from '../types';
import { DASHBOARD_TABS } from '../constants';

interface NavigationProps {
    selectedTab: DashboardTab;
    setSelectedTab: (tab: DashboardTab) => void;
}

export default function Navigation({ selectedTab, setSelectedTab }: NavigationProps) {
    return (
        <div className="fixed sm:sticky sm:top-0 bottom-0 sm:bottom-auto w-full z-20 bg-white dark:bg-navy-900 backdrop-blur-xl border-t sm:border-t-0 sm:border-b border-navy-200 dark:border-navy-700">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <nav className="flex justify-around sm:justify-start sm:space-x-8">
                    {DASHBOARD_TABS.map(({ id, label, icon: Icon }) => (
                        <button
                            key={id}
                            onClick={() => setSelectedTab(id)}
                            className={`
                relative flex flex-col sm:flex-row items-center px-2 sm:px-4 py-3 sm:py-4 text-sm font-medium 
                transition-all duration-200
                ${
                    selectedTab === id
                        ? 'text-navy-600 dark:text-navy-300'
                        : 'text-navy-500 hover:text-navy-700 dark:text-navy-400 dark:hover:text-white'
                }
              `}
                        >
                            <Icon className="w-6 h-6 sm:w-5 sm:h-5" />
                            <span className="text-[10px] mt-1 sm:text-sm sm:mt-0 sm:ml-2">
                                {label}
                            </span>
                            {selectedTab === id && (
                                <div className="absolute top-0 sm:top-auto sm:bottom-0 left-0 w-full h-0.5 sm:h-0.5 bg-navy-500" />
                            )}
                        </button>
                    ))}
                </nav>
            </div>
        </div>
    );
}
