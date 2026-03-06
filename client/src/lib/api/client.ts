/**
 * Fetch wrapper — 인증, 401 refresh/retry, 에러 파싱.
 * SoT: docs/api/contracts.md §11.2, §11.4
 */
import { getApiBase, API_BASE_PATH } from "./config";
import { ApiError } from "./errors";
import type { ApiErrorBody } from "@/types/api-contracts";

export interface ApiClientOptions {
  /** Bearer 토큰. 없으면 Authorization 미전송(health 등) */
  accessToken?: string | null;
  /** 401 시 refresh 시도 후 원 요청 재시도용 */
  onRefresh?: () => Promise<string | null>;
  /** refresh 실패 시 /login 리다이렉트 */
  onUnauthorized?: () => void;
}

async function parseErrorBody(res: Response): Promise<ApiErrorBody> {
  let body: unknown;
  try {
    const text = await res.text();
    body = text ? JSON.parse(text) : {};
  } catch {
    body = { code: "UNKNOWN", message: res.statusText };
  }
  const obj = body as Record<string, unknown>;
  return {
    code: (obj.code as string) ?? "UNKNOWN",
    message: (obj.message as string) ?? res.statusText,
    reasonCode: obj.reasonCode as string | undefined,
    details: obj.details as Record<string, unknown> | undefined,
  };
}

export interface RequestConfig extends RequestInit {
  /** 401 시 retry 건너뛰기( refresh 호출 자체에 사용) */
  skipAuthRetry?: boolean;
}

/**
 * 인증 포함 fetch. 401 시 onRefresh 1회 호출 후 원 요청 1회 재시도.
 */
export async function apiFetch<T>(
  path: string,
  options: RequestConfig & { parseJson?: (data: unknown) => T } = {},
  clientOpts: ApiClientOptions = {}
): Promise<T> {
  const { accessToken, onRefresh, onUnauthorized } = clientOpts;
  const { skipAuthRetry, parseJson, ...fetchOpts } = options;

  const base = getApiBase();
  const url = path.startsWith("http") ? path : `${base}${API_BASE_PATH}${path}`;

  const headers = new Headers(fetchOpts.headers);
  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }
  if (!headers.has("Content-Type") && fetchOpts.body && typeof fetchOpts.body === "string") {
    headers.set("Content-Type", "application/json");
  }

  const doFetch = async (): Promise<Response> => {
    return fetch(url, {
      ...fetchOpts,
      headers,
      credentials: path.includes("/auth/") ? "include" : "omit",
    });
  };

  let res = await doFetch();

  // 401 → refresh 1회 + 재시도 1회 (skipAuthRetry가 아닐 때만)
  if (res.status === 401 && !skipAuthRetry && onRefresh) {
    const newToken = await onRefresh();
    if (newToken) {
      headers.set("Authorization", `Bearer ${newToken}`);
      res = await doFetch();
    }
    if (res.status === 401) {
      onUnauthorized?.();
      const body = await parseErrorBody(res);
      throw new ApiError(res.status, body, res);
    }
  }

  if (!res.ok) {
    const body = await parseErrorBody(res);
    throw new ApiError(res.status, body, res);
  }

  const text = await res.text();
  if (!text) return undefined as T;
  const data = JSON.parse(text);
  return parseJson ? parseJson(data) : (data as T);
}
