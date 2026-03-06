/**
 * 알림 — 권한/푸시/구독/테스트 구조만.
 * 실 API 호출 금지(mock/placeholder).
 */
export default function NotificationsPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">알림</h1>

      <section aria-label="권한 상태" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">권한 상태 카드</h2>
        <p className="mt-2 text-sm text-text-3">Notification.permission (placeholder)</p>
      </section>

      <section aria-label="푸시 토글" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">푸시 토글</h2>
        <p className="mt-2 text-sm text-text-3">ON/OFF (placeholder)</p>
      </section>

      <section aria-label="구독 목록" className="rounded-lg border border-border bg-bg2 p-4">
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
              <tr className="border-b border-divider">
                <td colSpan={3} className="py-6 text-center text-text-3">
                  (목록 placeholder)
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section aria-label="테스트 푸시" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">테스트 푸시</h2>
        <p className="mt-2 text-sm text-text-3">message + deepLink (placeholder)</p>
      </section>
    </div>
  );
}
