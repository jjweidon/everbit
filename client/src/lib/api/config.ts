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

/**
 * 카카오 OAuth2 시작 엔드포인트 (커스텀 — Spring Security 기본값 아님).
 * GET /api/v2/auth/start → state 생성 후 Kakao 인증 URL로 리다이렉트.
 */
export const KAKAO_AUTH_START_PATH = `${API_BASE_PATH}/auth/start`;

/**
 * 카카오 로그인 시작 URL 반환.
 * - NEXT_PUBLIC_API_BASE="" (프록시 모드): /api/v2/* rewrite → http://localhost:8080
 * - NEXT_PUBLIC_API_BASE=URL (직접 모드): 백엔드 절대 URL
 */
export function getKakaoAuthUrl(): string {
  return `${getApiBase()}${KAKAO_AUTH_START_PATH}`;
}
