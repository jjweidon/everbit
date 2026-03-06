# 개발 환경 CORS/프록시 설정

Status: **Draft**  
Owner: everbit  
Last updated: 2026-03-06

클라이언트(Next.js 3000) ↔ 서버(Spring Boot 8080) 연동 시 CORS/프록시 옵션.

---

## 1. 프록시 모드 (권장)

Next.js rewrites로 `/api/v2/*`를 Spring Boot로 프록시. CORS 불필요.

1. `client/.env.local` 생성:
   ```
   NEXT_PUBLIC_API_BASE=
   ```
2. `pnpm dev` 실행
3. 클라이언트 요청은 `localhost:3000/api/v2/...` → Next가 8080으로 프록시

---

## 2. 직접 호출 모드

클라이언트가 `localhost:8080`에 직접 요청. 서버 CORS 설정 필요.

1. `client/.env.local`:
   ```
   NEXT_PUBLIC_API_BASE=http://localhost:8080
   ```
2. 서버에 CORS 허용 추가 (예: `WebMvcConfigurer`):
   - `Origin: http://localhost:3000`
   - `Methods: GET, POST, PUT, DELETE, OPTIONS`
   - `Credentials: true` (auth/refresh 쿠키)

---

## 3. 운영

`NEXT_PUBLIC_API_BASE=https://api.everbit.kr` 등 실제 API URL로 설정.
