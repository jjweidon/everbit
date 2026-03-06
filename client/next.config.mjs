/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  /**
   * 개발 시 CORS 회피: /api/v2/* → Spring Boot(8080) 프록시.
   * NEXT_PUBLIC_API_BASE="" 로 설정 시 상대 경로 사용 → 이 rewrites 적용.
   */
  async rewrites() {
    return [
      {
        source: "/api/v2/:path*",
        destination: "http://localhost:8080/api/v2/:path*",
      },
    ];
  },
};

export default nextConfig;
