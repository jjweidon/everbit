/**
 * API 설정. SoT: docs/api/contracts.md §0.2
 * Base URL은 env로 분리. NEXT_PUBLIC_API_BASE 우선, API_BASE_URL 호환.
 *
 * - "" (빈 문자열): 상대 경로(프록시 모드, rewrites 사용)
 * - 미설정: http://localhost:8082 (로컬 개발 전용)
 * - 설정값: 해당 URL 사용 (Vercel 배포 시 https://api.everbit.kr 등 운영 URL 필수)
 */
const FALLBACK_BASE = "http://localhost:8082";

export function getApiBase(): string {
  const env =
    process.env.NEXT_PUBLIC_API_BASE ??
    process.env.NEXT_PUBLIC_API_BASE_URL;
  if (env === "") return "";
  return env ?? FALLBACK_BASE;
}

export const API_BASE_PATH = "/api/v2";

/**
 * Spring Security OAuth2 Client 기본 로그인 시작 엔드포인트.
 * GET /api/v2/oauth2/authorization/kakao → 카카오 인증 페이지로 리다이렉트.
 */
export const KAKAO_OAUTH2_AUTHORIZATION_PATH = "/api/v2/oauth2/authorization/kakao";

/**
 * 카카오 로그인 시작 URL. 로그인 버튼 클릭 시 이 URL로 브라우저 리다이렉트.
 */
export function getKakaoAuthUrl(): string {
  return `${getApiBase()}${KAKAO_OAUTH2_AUTHORIZATION_PATH}`;
}

/**
 * Spring Security 기본 로컬 로그아웃 URL. 이동 시 Refresh 쿠키 무효화 후 프론트 /login으로 리다이렉트.
 */
export function getLogoutUrl(): string {
  return `${getApiBase()}/logout`;
}
