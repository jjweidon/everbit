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
};

module.exports = nextConfig;
