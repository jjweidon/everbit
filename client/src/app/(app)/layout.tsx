import { AppShell } from "@/components/layout/AppShell";
import { AuthProvider } from "@/contexts/AuthContext";
import { ToastProvider } from "@/components/ui/Toast";

export default function AppLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AuthProvider>
      <ToastProvider>
        <AppShell>{children}</AppShell>
      </ToastProvider>
    </AuthProvider>
  );
}
