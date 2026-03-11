/**
 * API 설정. SoT: docs/api/contracts.md §0.2
 * Base URL은 env로 분리. NEXT_PUBLIC_API_BASE 우선, API_BASE_URL 호환.
 *
 * - "" (빈 문자열): 상대 경로(프록시 모드, rewrites 사용)
 * - 미설정: http://localhost:8080 (로컬 개발 전용)
 * - 설정값: 해당 URL 사용 (Vercel 배포 시 https://api.everbit.kr 등 운영 URL 필수)
 */
const FALLBACK_BASE = "http://localhost:8080";

export function getApiBase(): string {
  const env =
    process.env.NEXT_PUBLIC_API_BASE ??
    process.env.NEXT_PUBLIC_API_BASE_URL;
  if (env === "") return "";
  return env ?? FALLBACK_BASE;
}

export const API_BASE_PATH = "/api/v2";
