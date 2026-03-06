import type { OrderListItem, OrderDetail } from "@/types/api-contracts";

export const mockOrderList: OrderListItem[] = [
  {
    intentPublicId: "01JCEXAMPLE001",
    createdAt: "2026-03-06T03:45:00.000Z",
    market: "KRW-BTC",
    side: "BUY",
    intentType: "ENTRY",
    requestedKrw: "500000",
    reasonCode: "SIGNAL_STRENGTH_HIGH",
    latestAttempt: {
      attemptPublicId: "01JCEXAMPLE002",
      attemptNo: 1,
      status: "ACKED",
      upbitUuid: "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    },
  },
  {
    intentPublicId: "01JCEXAMPLE003",
    createdAt: "2026-03-06T03:30:00.000Z",
    market: "KRW-ETH",
    side: "SELL",
    intentType: "EXIT_TP",
    requestedVolume: "0.5",
    reasonCode: "TP_HIT",
    latestAttempt: {
      attemptPublicId: "01JCEXAMPLE004",
      attemptNo: 1,
      status: "THROTTLED",
      nextRetryAt: "2026-03-06T03:35:00.000Z",
    },
  },
  {
    intentPublicId: "01JCEXAMPLE005",
    createdAt: "2026-03-06T02:00:00.000Z",
    market: "KRW-BTC",
    side: "BUY",
    intentType: "ENTRY",
    requestedKrw: "300000",
    latestAttempt: {
      attemptPublicId: "01JCEXAMPLE006",
      attemptNo: 1,
      status: "UNKNOWN",
      errorCode: "TIMEOUT",
    },
  },
  {
    intentPublicId: "01JCEXAMPLE007",
    createdAt: "2026-03-06T01:20:00.000Z",
    market: "KRW-XRP",
    side: "BUY",
    intentType: "ENTRY",
    requestedKrw: "200000",
    latestAttempt: {
      attemptPublicId: "01JCEXAMPLE008",
      attemptNo: 1,
      status: "ACKED",
      upbitUuid: "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    },
  },
];

export const mockOrderDetail: OrderDetail = {
  upbitUuid: "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  state: "done",
  ordType: "price",
  side: "BUY",
  price: "95000000",
  volume: "0.00526",
  executedVolume: "0.00526",
  intent: {
    intentType: "ENTRY",
    requestedKrw: "500000",
    reasonCode: "SIGNAL_STRENGTH_HIGH",
  },
  attempts: [
    {
      attemptPublicId: "01JCEXAMPLE002",
      attemptNo: 1,
      status: "ACKED",
      upbitUuid: "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      createdAt: "2026-03-06T03:45:01.000Z",
    },
  ],
  fills: [
    {
      tradeTime: "2026-03-06T03:45:02.000Z",
      price: "95000000",
      volume: "0.00526",
      fee: "475",
    },
  ],
};
