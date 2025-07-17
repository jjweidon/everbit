'use client';

import { useState, useEffect } from 'react';

interface TradeLog {
  id: string;
  userId: string;
  symbol: string;
  side: 'BUY' | 'SELL';
  price: number;
  quantity: number;
  timestamp: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED';
}

export default function TradeLog() {
  const [logs, setLogs] = useState<TradeLog[]>([]);
  const [isLive, setIsLive] = useState(false);

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
      const response = await fetch('/api/admin/trade-logs');
      const data = await response.json();
      setLogs(data);
    } catch (error) {
      console.error('Failed to fetch trade logs:', error);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white dark:bg-navy-800 shadow-lg rounded-lg overflow-hidden">
        <div className="px-6 py-4 border-b border-navy-200 dark:border-navy-700 flex justify-between items-center">
          <h3 className="text-lg font-semibold text-navy-900 dark:text-white">매매 로그</h3>
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
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-navy-50 dark:bg-navy-900">
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">시간</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">사용자 ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">심볼</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">거래 유형</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">가격</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">수량</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">상태</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-navy-200 dark:divide-navy-700">
              {logs.map((log) => (
                <tr key={log.id} className="hover:bg-navy-50 dark:hover:bg-navy-700/50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-900 dark:text-navy-100">
                    {new Date(log.timestamp).toLocaleString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-900 dark:text-navy-100">{log.userId}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-900 dark:text-navy-100">{log.symbol}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <span
                      className={`px-2 py-1 rounded-full text-xs font-medium ${
                        log.side === 'BUY'
                          ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                          : 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                      }`}
                    >
                      {log.side === 'BUY' ? '매수' : '매도'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-900 dark:text-navy-100">
                    {log.price.toLocaleString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-900 dark:text-navy-100">{log.quantity}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <span
                      className={`px-2 py-1 rounded-full text-xs font-medium ${
                        log.status === 'COMPLETED'
                          ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                          : log.status === 'FAILED'
                          ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                          : 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200'
                      }`}
                    >
                      {log.status === 'COMPLETED'
                        ? '완료'
                        : log.status === 'FAILED'
                        ? '실패'
                        : '대기중'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
} 