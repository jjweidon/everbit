/**
 * API 계약 타입 정의 (실 API 연동 전 mock용)
 * SoT: docs/ui/everbit_ui_impl_spec.md §7, docs/api/contracts.md
 */

// --- 공통 ---
/** 표준 에러 응답 본문 (docs/api/contracts.md §9) */
export interface ApiErrorBody {
  code: string;
  message: string;
  reasonCode?: string;
  details?: Record<string, unknown>;
}

/** POST /api/v2/auth/refresh 응답 (ADR-0007) */
export interface AuthRefreshResponse {
  accessToken: string;
}

// --- Dashboard ---
export type WsStatus = "CONNECTED" | "DEGRADED" | "DISCONNECTED";

export interface DashboardSummary {
  accountEnabled: boolean;
  strategyKey: "EXTREME_FLIP";
  strategyEnabled: boolean;
  wsStatus: WsStatus;
  lastReconcileAt?: string;
  lastErrorAt?: string;
  risk: {
    throttled429Count24h: number;
    blocked418Until?: string;
    unknownAttempts24h: number;
    suspendedMarkets: string[];
  };
  equity: {
    equityKrw: string;
    realizedPnlKrw: string;
    unrealizedPnlKrw: string;
  };
}

// --- Order ---
export type OrderSide = "BUY" | "SELL";
export type IntentType =
  | "ENTRY"
  | "EXIT_STOPLOSS"
  | "EXIT_TP"
  | "EXIT_TRAIL"
  | "EXIT_TIME";
export type AttemptStatus =
  | "PREPARED"
  | "SENT"
  | "ACKED"
  | "REJECTED"
  | "THROTTLED"
  | "UNKNOWN"
  | "SUSPENDED";

export interface OrderListItem {
  intentPublicId: string;
  createdAt: string;
  market: string;
  side: OrderSide;
  intentType: IntentType;
  requestedKrw?: string;
  requestedVolume?: string;
  reasonCode?: string;
  latestAttempt: {
    attemptPublicId: string;
    attemptNo: number;
    status: AttemptStatus;
    upbitUuid?: string;
    nextRetryAt?: string;
    errorCode?: string;
    errorMessage?: string;
  };
}

/** GET /api/v2/orders 응답 */
export interface OrderListResponse {
  items: OrderListItem[];
  nextCursor: string | null;
}

export interface OrderAttemptItem {
  attemptPublicId: string;
  attemptNo: number;
  status: AttemptStatus;
  upbitUuid?: string;
  nextRetryAt?: string;
  errorCode?: string;
  errorMessage?: string;
  createdAt: string;
}

export interface OrderDetail {
  upbitUuid: string;
  state: "wait" | "done" | "cancel";
  ordType: string;
  side: OrderSide;
  price?: string;
  volume?: string;
  executedVolume?: string;
  intent?: {
    intentType: IntentType;
    requestedKrw?: string;
    requestedVolume?: string;
    reasonCode?: string;
  };
  attempts: OrderAttemptItem[];
  fills: {
    tradeTime: string;
    price: string;
    volume: string;
    fee?: string;
  }[];
}

// --- Market ---
export type PositionStatus = "FLAT" | "OPEN" | "SUSPENDED";

export interface MarketStatusItem {
  market: string;
  enabled: boolean;
  priority: number;
  positionStatus: PositionStatus;
  lastSignalAt?: string;
  cooldownUntil?: string;
}

// --- Strategy ---
export interface StrategyConfig {
  strategyKey: "EXTREME_FLIP";
  configVersion: number;
  updatedAt: string;
  configJson: Record<string, unknown>;
}

// --- Backtest ---
export type BacktestJobStatus = "QUEUED" | "RUNNING" | "DONE" | "FAILED";

export interface BacktestJobItem {
  jobPublicId: string;
  status: BacktestJobStatus;
  createdAt: string;
  markets: string[];
  timeframes: string[];
}

export interface BacktestDetail extends BacktestJobItem {
  metrics?: {
    cagr?: number;
    mdd?: number;
    winRate?: number;
    profitFactor?: number;
  };
  requestJson?: Record<string, unknown>;
}

// --- Notifications ---
export interface PushSubscriptionItem {
  id: string;
  endpointMasked: string;
  userAgent: string;
  enabled: boolean;
}

// --- Upbit Key ---
export type UpbitKeyStatus = "REGISTERED" | "NOT_REGISTERED" | "VERIFICATION_FAILED";

export interface UpbitKeyStatusResponse {
  status: UpbitKeyStatus;
  lastVerifiedAt?: string;
  verificationErrorCode?: string;
}
