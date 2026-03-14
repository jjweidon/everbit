"use client";

import { useState } from "react";
import { usePathname } from "next/navigation";
import { Sidebar, MobileNavContent } from "./Sidebar";
import { Topbar } from "./Topbar";
import { Footer } from "./Footer";
import { Drawer } from "@/components/ui/Drawer";

interface AppShellProps {
  children: React.ReactNode;
}

/**
 * 전역 레이아웃: 최상위 Header(전체 너비) / [Sidebar + Main] / Footer(전체 너비).
 * 헤더·푸터는 사이드바 영향 없이 전체 폭. 메인 영역만 사이드바와 분리.
 * docs/ui/everbit_ui_impl_spec.md §4.1 AppShell
 */
export function AppShell({ children }: AppShellProps) {
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const pathname = usePathname();

  return (
    <div className="flex min-h-screen flex-col bg-bg1 text-text-1">
      {/* 1) 헤더: 전체 너비 (사이드바와 무관) */}
      <Topbar onOpenMobileNav={() => setMobileNavOpen(true)} />

      {/* 2) 메인 영역: 사이드바(전체 높이) + 콘텐츠 */}
      <div className="flex flex-1 min-h-0 items-stretch">
        <div className="hidden md:flex flex-col min-h-0 shrink-0 self-stretch w-14">
          <Sidebar />
        </div>
        <main className="min-w-0 flex-1 overflow-auto" id="main-content">
          <div className="mx-auto w-full max-w-7xl p-4 md:p-6">{children}</div>
        </main>
      </div>

      {/* 3) 푸터: 전체 너비 */}
      <Footer />

      <Drawer
        open={mobileNavOpen}
        onClose={() => setMobileNavOpen(false)}
        side="left"
        width={280}
        title="메뉴"
      >
        <div className="flex flex-col pt-2">
          <MobileNavContent
            currentPathname={pathname}
            onNavigate={() => setMobileNavOpen(false)}
          />
        </div>
      </Drawer>
    </div>
  );
}
