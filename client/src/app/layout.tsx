import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "에버비트",
  description: "암호화폐 퀀트 트레이딩 시스템",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className="min-h-screen bg-bg1 text-text-1 antialiased">
        {children}
      </body>
    </html>
  );
}
