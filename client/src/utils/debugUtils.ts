import { useAuthStore, getExpirationFromToken } from '@/store/authStore';

/**
 * 쿠키값 가져오는 유틸리티 함수
 */
const getCookie = (name: string): string | null => {
  if (typeof document === 'undefined') return null;
  
  const cookies = document.cookie.split('; ').reduce((acc: Record<string, string>, cookie) => {
    const [cookieName, cookieValue] = cookie.split('=');
    acc[cookieName] = cookieValue;
    return acc;
  }, {});
  
  return cookies[name] || null;
};

/**
 * 토큰 정보 디버깅 함수
 */
export const debugToken = (): void => {
  console.log('----- 토큰 디버깅 -----');
  
  // 브라우저 환경 체크
  if (typeof window === 'undefined') {
    console.log('브라우저 환경이 아닙니다.');
    return;
  }
  
  // 로컬 스토리지 확인
  const lsAuthStatus = localStorage.getItem('AuthStatus');
  const lsToken = localStorage.getItem('Authorization');
  
  console.log('[로컬스토리지] 인증 상태:', lsAuthStatus || '없음');
  console.log('[로컬스토리지] 토큰 존재:', lsToken ? '있음' : '없음');
  
  // 쿠키 확인
  const cookieAuthStatus = getCookie('AuthStatus');
  const cookieToken = getCookie('Authorization');
  
  console.log('[쿠키] 인증 상태:', cookieAuthStatus || '없음');
  console.log('[쿠키] 토큰 존재:', cookieToken ? '있음' : '없음');
  
  // Zustand 스토어 확인
  const authState = useAuthStore.getState();
  console.log('[스토어] 인증 상태:', authState.status);
  console.log('[스토어] 토큰 존재:', authState.token ? '있음' : '없음');
  
  // 전체 쿠키 내용 로깅
  console.log('[쿠키] 전체 쿠키:', document.cookie);
  
  // 토큰 확인 (로컬 스토리지 또는 쿠키)
  const token = lsToken || cookieToken;
  
  // 토큰이 없으면 종료
  if (!token) {
    console.log('토큰이 로컬 스토리지와 쿠키 모두에 없습니다.');
    console.log('----- 디버깅 종료 -----');
    return;
  }
  
  try {
    // JWT 토큰 구조: header.payload.signature
    const tokenParts = token.split('.');
    if (tokenParts.length !== 3) {
      console.log('토큰 구조가 올바르지 않습니다. (header.payload.signature 형식이 아님)');
      return;
    }
    
    // Base64 디코딩
    const header = JSON.parse(atob(tokenParts[0]));
    const payload = JSON.parse(atob(tokenParts[1]));
    
    console.log('토큰 헤더:', header);
    console.log('토큰 페이로드:', payload);
    
    // 만료 시간 확인
    if (payload.exp) {
      const expiresAt = new Date(payload.exp * 1000);
      const now = new Date();
      const isExpired = now > expiresAt;
      
      console.log('토큰 만료 시간:', expiresAt.toLocaleString());
      console.log('현재 시간:', now.toLocaleString());
      console.log('만료 여부:', isExpired ? '만료됨' : '유효함');
      console.log('남은 시간:', isExpired ? '만료됨' : `${Math.floor((expiresAt.getTime() - now.getTime()) / 1000 / 60)}분`);
    } else {
      console.log('토큰에 만료 시간(exp)이 없습니다.');
    }
  } catch (error) {
    console.error('토큰 디코딩 중 오류 발생:', error);
  }
  
  console.log('----- 디버깅 종료 -----');
};

/**
 * 현재 페이지 URL 및 쿼리 파라미터를 콘솔에 출력하는 함수
 */
export const debugCurrentUrl = (): void => {
  console.log('----- URL 디버깅 -----');
  
  // 브라우저 환경 체크
  if (typeof window === 'undefined') {
    console.log('브라우저 환경이 아닙니다.');
    return;
  }
  
  console.log('현재 URL:', window.location.href);
  console.log('호스트:', window.location.host);
  console.log('경로:', window.location.pathname);
  
  // 쿼리 파라미터 추출
  const searchParams = new URLSearchParams(window.location.search);
  const params: Record<string, string> = {};
  searchParams.forEach((value, key) => {
    params[key] = value;
  });
  
  if (Object.keys(params).length > 0) {
    console.log('쿼리 파라미터:', params);
    searchParams.forEach((value, key) => {
      console.log(`  ${key}: ${value}`);
    });
  } else {
    console.log('쿼리 파라미터: 없음');
  }
  
  console.log('----- 디버깅 종료 -----');
}; 