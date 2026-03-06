/**
 * API 엔드포인트 — 계약 기반 typed 호출.
 * SoT: docs/api/contracts.md
 */
import { apiFetch } from "./client";
import type {
  DashboardSummary,
  OrderListResponse,
  OrderListItem,
  MarketStatusItem,
} from "@/types/api-contracts";

export interface GetOrdersParams {
  limit?: number;
  cursor?: string;
  market?: string;
  attemptStatus?: string;
  onlyAcked?: boolean;
}

export interface ApiEndpointsOptions {
  accessToken: string | null;
  onRefresh: () => Promise<string | null>;
  onUnauthorized: () => void;
}

function createClient(opts: ApiEndpointsOptions) {
  return {
    accessToken: opts.accessToken,
    onRefresh: opts.onRefresh,
    onUnauthorized: opts.onUnauthorized,
  };
}

export function getDashboardSummary(opts: ApiEndpointsOptions): Promise<DashboardSummary> {
  const client = createClient(opts);
  return apiFetch<DashboardSummary>("/dashboard/summary", { method: "GET" }, client);
}

export function getOrders(
  params: GetOrdersParams = {},
  opts: ApiEndpointsOptions
): Promise<OrderListResponse> {
  const client = createClient(opts);
  const search = new URLSearchParams();
  if (params.limit != null) search.set("limit", String(params.limit));
  if (params.cursor) search.set("cursor", params.cursor);
  if (params.market) search.set("market", params.market);
  if (params.attemptStatus) search.set("attemptStatus", params.attemptStatus);
  if (params.onlyAcked) search.set("onlyAcked", "true");
  const qs = search.toString();
  const path = `/orders${qs ? `?${qs}` : ""}`;
  return apiFetch<OrderListResponse>(path, { method: "GET" }, client);
}

export function getMarkets(opts: ApiEndpointsOptions): Promise<MarketStatusItem[]> {
  const client = createClient(opts);
  return apiFetch<MarketStatusItem[]>("/markets", { method: "GET" }, client);
}
