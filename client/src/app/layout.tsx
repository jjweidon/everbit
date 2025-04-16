import type { Metadata } from 'next';
import { Noto_Sans_KR } from 'next/font/google';
import './globals.css';

const notoSansKr = Noto_Sans_KR({
  subsets: ['latin'],
  weight: ['400', '500', '700'],
  display: 'swap',
  variable: '--font-noto-sans-kr',
  preload: true,
});

export const metadata: Metadata = {
  title: 'everbit - 비트코인 자동 트레이딩 시스템',
  description: 'Upbit API 기반 비트코인 자동 트레이딩 시스템',
  icons: {
    icon: '/favicon.ico',
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko" className={notoSansKr.variable}>
      <body className="font-sans bg-white text-navy-800">
        {children}
      </body>
    </html>
  );
} 