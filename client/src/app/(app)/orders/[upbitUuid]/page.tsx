/**
 * 주문 상세 — 딥링크 /orders/[upbitUuid]. 구조만.
 * 실 API 호출 금지(mock/placeholder).
 */
interface OrderDetailPageProps {
  params: Promise<{ upbitUuid: string }>;
}

export default async function OrderDetailPage({ params }: OrderDetailPageProps) {
  const { upbitUuid } = await params;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-text-1">주문 상세</h1>
      <p className="text-sm text-text-2">Upbit 주문 ID: {upbitUuid}</p>

      <section aria-label="Upbit 주문 스냅샷" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">Upbit 주문 스냅샷</h2>
        <div className="mt-3 grid grid-cols-2 gap-2 text-sm text-text-2 sm:grid-cols-4">
          state / ordType / side / price / volume (placeholder)
        </div>
      </section>

      <section aria-label="관련 주문 의도" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">관련 주문 의도</h2>
        <p className="mt-2 text-sm text-text-3">(placeholder)</p>
      </section>

      <section aria-label="시도 타임라인" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">시도 타임라인</h2>
        <p className="mt-2 text-sm text-text-3">(placeholder)</p>
      </section>

      <section aria-label="체결 내역" className="rounded-lg border border-border bg-bg2 p-4">
        <h2 className="text-sm font-medium text-text-2">체결 내역</h2>
        <p className="mt-2 text-sm text-text-3">(placeholder)</p>
      </section>
    </div>
  );
}
