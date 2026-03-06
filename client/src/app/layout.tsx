import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Everbit v2",
  description: "Everbit trading dashboard",
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
