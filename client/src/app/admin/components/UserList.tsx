'use client';

import { useState, useEffect } from 'react';
import { UserResponse } from '@/api/types';

export default function UserList() {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [selectedUser, setSelectedUser] = useState<string | null>(null);
  const [message, setMessage] = useState('');

  useEffect(() => {
    console.log("UserList fetchUsers");
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await fetch('/api/admin/users');
      const data = await response.json();
      setUsers(data);
    } catch (error) {
      console.error('Failed to fetch users:', error);
    }
  };

  const handleSendMessage = async () => {
    if (!selectedUser || !message) return;

    try {
      await fetch('/api/admin/messages', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userId: selectedUser,
          message: message,
        }),
      });
      setMessage('');
      setSelectedUser(null);
    } catch (error) {
      console.error('Failed to send message:', error);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white dark:bg-navy-800 shadow-lg rounded-lg overflow-hidden">
        <div className="px-6 py-4 border-b border-navy-200 dark:border-navy-700">
          <h3 className="text-lg font-semibold text-navy-900 dark:text-white">사용자 목록</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-navy-50 dark:bg-navy-900">
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">이메일</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">이름</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">가입일</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-navy-500 dark:text-navy-300 uppercase tracking-wider">액션</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-navy-200 dark:divide-navy-700">
              {users.map((user) => (
                <tr key={user.userId} className="hover:bg-navy-50 dark:hover:bg-navy-700/50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-900 dark:text-navy-100">{user.userId}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-900 dark:text-navy-100">{user.username}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-900 dark:text-navy-100">{user.nickname}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-navy-900 dark:text-navy-100">
                    {new Date(user.createdAt).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <button
                      onClick={() => setSelectedUser(user.userId)}
                      className="text-navy-600 dark:text-navy-300 hover:text-navy-900 dark:hover:text-white font-medium"
                    >
                      메시지 보내기
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {selectedUser && (
        <div className="bg-white dark:bg-navy-800 shadow-lg rounded-lg overflow-hidden">
          <div className="px-6 py-4 border-b border-navy-200 dark:border-navy-700">
            <h3 className="text-lg font-semibold text-navy-900 dark:text-white">메시지 보내기</h3>
          </div>
          <div className="p-6 space-y-4">
            <textarea
              className="w-full rounded-lg border border-navy-200 dark:border-navy-700 bg-white dark:bg-navy-900 p-3 text-navy-900 dark:text-white placeholder-navy-400 dark:placeholder-navy-500 focus:outline-none focus:ring-2 focus:ring-navy-500 dark:focus:ring-navy-400"
              rows={4}
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="메시지를 입력하세요..."
            />
            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setSelectedUser(null)}
                className="px-4 py-2 text-sm font-medium text-navy-600 dark:text-navy-300 hover:text-navy-900 dark:hover:text-white"
              >
                취소
              </button>
              <button
                onClick={handleSendMessage}
                className="px-4 py-2 text-sm font-medium text-white bg-navy-600 hover:bg-navy-700 dark:bg-navy-500 dark:hover:bg-navy-400 rounded-lg transition-colors"
              >
                보내기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
} 