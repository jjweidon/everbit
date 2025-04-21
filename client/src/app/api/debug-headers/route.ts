import { NextRequest, NextResponse } from 'next/server';

export async function GET(request: NextRequest) {
  const headers: Record<string, string> = {};
  
  // 요청 헤더 추출
  request.headers.forEach((value, key) => {
    headers[key] = value;
  });
  
  // 쿠키 추출
  const cookies: Record<string, string> = {};
  request.cookies.getAll().forEach(cookie => {
    cookies[cookie.name] = cookie.value;
  });
  
  // URL 및 검색 파라미터 추출
  const url = request.url;
  const searchParams: Record<string, string> = {};
  request.nextUrl.searchParams.forEach((value, key) => {
    searchParams[key] = value;
  });
  
  return NextResponse.json({
    success: true,
    message: '디버깅용 헤더 정보',
    data: {
      headers,
      cookies,
      url,
      searchParams
    }
  });
} 