'use client';

import MainHeader from '@/components/MainHeader';
import { useState } from 'react';

export default function MyPage() {
  const [botStatus, setBotStatus] = useState(false);

  return (
    <div className="min-h-screen bg-navy-800">
      <MainHeader title="My Page" botStatus={botStatus} />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 마이페이지 컨텐츠 */}
      </div>
    </div>
  );
} 