/**
 * 알림 — 권한/푸시/구독/테스트
 * docs/ui/everbit_ui_impl_spec.md §5.9
 * Mock 데이터 사용, 실 API 호출 금지
 */
"use client";

import { useState } from "react";
import { OnOffBadge } from "@/components/ui";
import { mockPushSubscriptions } from "@/lib/mocks/notifications";

export default function NotificationsPage() {
  const [permission] = useState<NotificationPermission | "default">("default");

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">알림</h1>

      <section aria-label="권한 상태" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">권한 상태 카드</h2>
        <div className="mt-3 flex items-center gap-2">
          <span
            className={`h-2 w-2 rounded-full ${
              permission === "granted" ? "bg-green" : permission === "denied" ? "bg-red" : "bg-yellow"
            }`}
            aria-hidden
          />
          <span className="text-sm text-text-2">
            Notification.permission: {permission === "default" ? "default (미확인)" : permission}
          </span>
        </div>
        {permission === "denied" && (
          <p className="mt-2 text-xs text-text-3">
            브라우저 설정에서 알림 권한을 허용해 주세요.
          </p>
        )}
      </section>

      <section aria-label="푸시 토글" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">푸시 토글</h2>
        <p className="mt-2 text-sm text-text-3">ON/OFF (API 연동 후 구현)</p>
      </section>

      <section aria-label="구독 목록" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">구독 목록</h2>
        <div className="mt-3 overflow-x-auto">
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="border-b border-divider text-left text-text-3">
                <th className="py-2 pr-4">엔드포인트</th>
                <th className="py-2 pr-4">브라우저</th>
                <th className="py-2 pr-4">사용</th>
              </tr>
            </thead>
            <tbody className="text-text-2">
              {mockPushSubscriptions.map((s) => (
                <tr key={s.id} className="border-b border-divider">
                  <td className="py-2 pr-4 font-mono text-xs text-text-3">{s.endpointMasked}</td>
                  <td className="py-2 pr-4">{s.userAgent}</td>
                  <td className="py-2 pr-4">
                    <OnOffBadge value={s.enabled} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section aria-label="테스트 푸시" className="rounded-lg border border-thin border-borderSubtle bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">테스트 푸시</h2>
        <p className="mt-2 text-sm text-text-3">message + deepLink (API 연동 후 구현)</p>
      </section>
    </div>
  );
}
