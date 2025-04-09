/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  output: 'export',
  images: {
    domains: ['localhost'],
    unoptimized: true,
  },
  // 정적 사이트 생성을 위한 추가 설정
  trailingSlash: true,
  assetPrefix: '/',
  basePath: '',
  
  // 폰트 최적화 설정
  optimizeFonts: true,
  
  // 실험적 기능 활성화
  experimental: {
    optimizeFonts: true,
  },
};

module.exports = nextConfig;
