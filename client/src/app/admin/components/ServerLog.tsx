'use client';

import { useState, useEffect } from 'react';

interface LogEntry {
  id: string;
  timestamp: string;
  level: 'INFO' | 'WARN' | 'ERROR';
  message: string;
  source: string;
}

export default function ServerLog() {
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [isLive, setIsLive] = useState(false);
  const [filter, setFilter] = useState<'INFO' | 'WARN' | 'ERROR' | 'ALL'>('ALL');

  useEffect(() => {
    fetchLogs();
  }, []);

  useEffect(() => {
    let interval: NodeJS.Timeout;

    if (isLive) {
      interval = setInterval(fetchLogs, 5000);
    }

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, [isLive]);

  const fetchLogs = async () => {
    try {
      const response = await fetch('/api/admin/server-logs');
      const data = await response.json();
      setLogs(data);
    } catch (error) {
      console.error('Failed to fetch server logs:', error);
    }
  };

  const filteredLogs = filter === 'ALL' 
    ? logs 
    : logs.filter(log => log.level === filter);

  const getLevelColor = (level: string) => {
    switch (level) {
      case 'INFO':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
      case 'WARN':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
      case 'ERROR':
        return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200';
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white dark:bg-navy-800 shadow-lg rounded-lg overflow-hidden">
        <div className="px-6 py-4 border-b border-navy-200 dark:border-navy-700 flex justify-between items-center">
          <h3 className="text-lg font-semibold text-navy-900 dark:text-white">서버 로그</h3>
          <div className="flex items-center space-x-4">
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value as any)}
              className="rounded-lg border border-navy-200 dark:border-navy-700 bg-white dark:bg-navy-900 px-3 py-1.5 text-sm text-navy-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-navy-500 dark:focus:ring-navy-400"
            >
              <option value="ALL">전체</option>
              <option value="INFO">INFO</option>
              <option value="WARN">WARN</option>
              <option value="ERROR">ERROR</option>
            </select>
            <label className="flex items-center space-x-2">
              <input
                type="checkbox"
                checked={isLive}
                onChange={(e) => setIsLive(e.target.checked)}
                className="rounded border-navy-300 dark:border-navy-600 text-navy-600 dark:text-navy-300 focus:ring-navy-500 dark:focus:ring-navy-400"
              />
              <span className="text-sm text-navy-600 dark:text-navy-300">실시간 업데이트</span>
            </label>
          </div>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-navy-50 dark:bg-navy-900">
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">시간</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">레벨</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">소스</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">메시지</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-navy-200 dark:divide-navy-700">
              {filteredLogs.map((log) => (
                <tr key={log.id} className="hover:bg-navy-50 dark:hover:bg-navy-700/50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-900 dark:text-navy-100">
                    {new Date(log.timestamp).toLocaleString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getLevelColor(log.level)}`}>
                      {log.level}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-900 dark:text-navy-100">{log.source}</td>
                  <td className="px-6 py-4 text-sm text-navy-900 dark:text-navy-100 font-mono">{log.message}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
} 