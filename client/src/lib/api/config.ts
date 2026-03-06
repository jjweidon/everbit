/**
 * API 설정. SoT: docs/api/contracts.md §0.2
 * Base URL은 env NEXT_PUBLIC_API_BASE로 분리.
 *
 * - "" (빈 문자열): 상대 경로(프록시 모드, rewrites 사용)
 * - 미설정: http://localhost:8080 (직접 호출, 서버 CORS 필요)
 * - 설정값: 해당 URL 사용
 */
const FALLBACK_BASE = "http://localhost:8080";

export function getApiBase(): string {
  const env = process.env.NEXT_PUBLIC_API_BASE;
  if (env === "") return "";
  return env ?? FALLBACK_BASE;
}

export const API_BASE_PATH = "/api/v2";
