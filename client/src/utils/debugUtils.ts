/**
 * 로컬 스토리지에서 토큰을 디코딩하고 콘솔에 출력하는 함수
 */
export const debugToken = (): void => {
  console.log('----- 토큰 디버깅 -----');
  
  // 브라우저 환경 체크
  if (typeof window === 'undefined') {
    console.log('브라우저 환경이 아닙니다.');
    return;
  }
  
  // 로컬 스토리지에서 토큰 확인
  const authToken = localStorage.getItem('Authorization');
  const authStatus = localStorage.getItem('AuthStatus');
  
  console.log('인증 상태:', authStatus);
  console.log('토큰 존재:', authToken ? '있음' : '없음');
  
  // 토큰이 없으면 종료
  if (!authToken) {
    console.log('토큰이 없습니다.');
    console.log('----- 디버깅 종료 -----');
    return;
  }
  
  try {
    // JWT 토큰 구조: header.payload.signature
    const tokenParts = authToken.split('.');
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
  console.log('쿼리 파라미터:');
  searchParams.forEach((value, key) => {
    console.log(`  ${key}: ${value}`);
  });
  
  console.log('----- 디버깅 종료 -----');
}; 