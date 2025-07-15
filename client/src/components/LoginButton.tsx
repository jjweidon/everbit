import React from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/useAuth';
import KakaoIcon from '@/components/icons/KakaoIcon';
import NaverIcon from '@/components/icons/NaverIcon';
import { loginApi } from '@/api/loginApi';

interface LoginButtonProps {
  provider: 'kakao' | 'naver';
  className?: string;
}

/**
 * 소셜 로그인 버튼 컴포넌트
 */
export default function LoginButton({ provider, className = '' }: LoginButtonProps) {
  const router = useRouter();
  const { isAuthenticated } = useAuth();

  // 이미 로그인된 경우 대시보드로 이동
  if (isAuthenticated) {
    router.push('/dashboard');
    return null;
  }

  const handleLogin = async () => {
    try {
      // 로그인 상태 로깅
      console.log(`${provider} 로그인 시작`);
      
      loginApi.kakaoLogin();
    } catch (error) {
      console.error(`${provider} 로그인 오류:`, error);
    }
  };

  // 제공자에 따른 스타일 및 텍스트 설정
  const getProviderStyle = () => {
    switch (provider) {
      case 'kakao':
        return {
          bg: 'bg-[#FEE500]',
          hoverBg: 'hover:bg-[#EBD71E]',
          text: 'text-[#3C1E1E]',
          label: '카카오 로그인',
          icon: <KakaoIcon />
        };
      case 'naver':
        return {
          bg: 'bg-[#03CF5D]',
          hoverBg: 'hover:bg-[#14D96B]',
          text: 'text-white',
          label: '네이버 로그인',
          icon: <NaverIcon />
        };
      default:
        return {
          bg: 'bg-gray-500',
          hoverBg: 'hover:bg-gray-600',
          text: 'text-white',
          label: '로그인',
          icon: null
        };
    }
  };

  const style = getProviderStyle();

  return (
    <button
      onClick={handleLogin}
      className={`w-full flex items-center justify-center space-x-2 px-4 py-3 ${style.bg} ${style.text} rounded-lg font-medium ${style.hoverBg} transition-colors ${className}`}
    >
      {style.icon}
      <span>{style.label}</span>
    </button>
  );
} 