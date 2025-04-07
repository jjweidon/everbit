import { NextRequest, NextResponse } from 'next/server';

// 서버 측에서만 사용하는 API URL (클라이언트에 노출되지 않음)
const API_URL = process.env.API_URL || 'http://localhost:8080/api';

export async function GET(request: NextRequest) {
  const { searchParams } = new URL(request.url);
  const path = searchParams.get('path') || '';
  
  try {
    const response = await fetch(`${API_URL}/${path}`);
    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('API 프록시 오류:', error);
    return NextResponse.json(
      { error: '서버 오류가 발생했습니다.' },
      { status: 500 }
    );
  }
}

export async function POST(request: NextRequest) {
  const { searchParams } = new URL(request.url);
  const path = searchParams.get('path') || '';
  
  try {
    const body = await request.json();
    const response = await fetch(`${API_URL}/${path}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });
    
    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('API 프록시 오류:', error);
    return NextResponse.json(
      { error: '서버 오류가 발생했습니다.' },
      { status: 500 }
    );
  }
} 