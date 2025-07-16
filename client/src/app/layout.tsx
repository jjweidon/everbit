import type { Metadata } from 'next';
import localFont from 'next/font/local';
import './globals.css';
import RouteGuard from '@/components/RouteGuard';

const logoFont = localFont({
  src: '../../public/fonts/logo_font.ttf',
  variable: '--font-logo',
  display: 'swap',
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
    <html lang="ko" suppressHydrationWarning className={`${logoFont.variable}`}>
      <head>
        <meta name="color-scheme" content="light only" />
      </head>
      <body className="font-sans bg-white text-navy-800">
        <RouteGuard>
          {children}
        </RouteGuard>
      </body>
    </html>
  );
} 