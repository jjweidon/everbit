import { NextResponse } from 'next/server';
import crypto from 'crypto';
import { v4 as uuidv4 } from 'uuid';
import axios from 'axios';

/**
 * 업비트 API 인증 헤더 생성
 */
const generateAuthToken = () => {
  const accessKey = process.env.UPBIT_ACCESS_KEY;
  const secretKey = process.env.UPBIT_SECRET_KEY;
  
  if (!accessKey || !secretKey) {
    throw new Error('업비트 API 키가 설정되지 않았습니다.');
  }
  
  const payload = {
    access_key: accessKey,
    nonce: uuidv4(),
  };
  
  const token = crypto
    .createHmac('sha256', secretKey)
    .update(JSON.stringify(payload))
    .digest('base64');
  
  const authorizationToken = `Bearer ${Buffer.from(
    JSON.stringify(payload)
  ).toString('base64')}.${token}`;
  
  return authorizationToken;
};

export async function GET() {
  try {
    const authToken = generateAuthToken();
    
    const response = await axios.get('https://api.upbit.com/v1/accounts', {
      headers: {
        Authorization: authToken,
      },
    });
    
    return NextResponse.json(response.data);
  } catch (error) {
    console.error('업비트 API 오류:', error);
    
    if (axios.isAxiosError(error) && error.response) {
      return NextResponse.json(
        { error: '업비트 API 요청 실패', details: error.response.data },
        { status: error.response.status }
      );
    }
    
    return NextResponse.json(
      { error: '서버 오류가 발생했습니다.' },
      { status: 500 }
    );
  }
} 