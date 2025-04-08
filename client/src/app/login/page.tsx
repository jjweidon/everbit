'use client';

import { Box, Container, Flex, Heading, Text, Image, Button } from '@chakra-ui/react';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

export default function LoginPage() {
  const router = useRouter();

  useEffect(() => {
    // 토큰이 있는 경우 대시보드로 리다이렉트
    const token = localStorage.getItem('upbit_access_token');
    if (token) {
      router.push('/dashboard');
    }
  }, [router]);

  const handleKakaoLogin = () => {
    // 카카오 로그인 URL로 리다이렉트
    window.location.href = 'https://www.everbit.kr/login/oauth2/code/kakao';
  };

  return (
    <Box bg="navy.800" minH="100vh">
      <Container maxW="container.md" py={20}>
        <Flex
          direction="column"
          align="center"
          justify="center"
          gap={8}
          bg="white"
          p={10}
          borderRadius="xl"
          boxShadow="0 4px 12px rgba(41,62,125,0.1)"
          border="1px solid"
          borderColor="navy.100"
        >
          <Heading 
            as="h1" 
            size="2xl" 
            color="navy.700"
            textAlign="center"
            mb={4}
          >
            everbit
          </Heading>
          
          <Text 
            fontSize="lg" 
            color="navy.600" 
            textAlign="center"
            maxW="md"
            mb={8}
          >
            비트코인 자동 트레이딩 시스템에 오신 것을 환영합니다.
            <br />
            로그인하여 서비스를 이용해보세요.
          </Text>

          <Box
            as="button"
            onClick={handleKakaoLogin}
            cursor="pointer"
            transition="all 0.2s"
            _hover={{
              transform: 'translateY(-2px)',
              boxShadow: '0 6px 12px rgba(41,62,125,0.2)'
            }}
          >
            <Image
              src="/images/kakao_login_button.png"
              alt="카카오 로그인"
              w="300px"
              h="auto"
            />
          </Box>

          <Text 
            fontSize="sm" 
            color="navy.500"
            mt={8}
            textAlign="center"
          >
            로그인 시 everbit의 이용약관과 개인정보 처리방침에 동의하게 됩니다.
          </Text>
        </Flex>
      </Container>
    </Box>
  );
} 