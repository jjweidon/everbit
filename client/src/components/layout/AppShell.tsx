import { Sidebar } from "./Sidebar";
import { Topbar } from "./Topbar";

interface AppShellProps {
  children: React.ReactNode;
}

/**
 * 전역 레이아웃: Sidebar + Topbar + main.
 * docs/ui/everbit_ui_impl_spec.md §4.1 AppShell
 */
export function AppShell({ children }: AppShellProps) {
  return (
    <div className="flex min-h-screen flex-col bg-bg1 text-text-1">
      <div className="flex flex-1">
        <Sidebar />
        <div className="flex min-w-0 flex-1 flex-col">
          <Topbar />
          <main className="flex-1 overflow-auto p-4" id="main-content">
            {children}
          </main>
        </div>
      </div>
    </div>
  );
}
