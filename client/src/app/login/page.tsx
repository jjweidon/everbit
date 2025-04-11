'use client';

import { Box, Container, Flex, Heading, Text, Image, VStack, Divider, useColorModeValue } from '@chakra-ui/react';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { loginApi } from '@/api/login';

export default function LoginPage() {
  const router = useRouter();
  const cardBg = useColorModeValue('white', 'gray.800');
  const borderColor = useColorModeValue('navy.100', 'navy.700');

  useEffect(() => {
    // 토큰이 있는 경우 대시보드로 리다이렉트
    const token = localStorage.getItem('upbit_access_token');
    if (token) {
      router.push('/dashboard');
    }
  }, [router]);

  const handleKakaoLogin = async () => {
    try {
      await loginApi.kakaoLogin();
    } catch (error) {
      console.error('카카오 로그인 에러:', error);
    }
  };

  return (
    <Box bg="gray.100" minH="100vh" position="fixed" top={0} left={0} right={0} bottom={0}>
      <Container maxW="container.sm" h="100vh" display="flex" alignItems="center">
        <Box
          w="100%"
          bg={cardBg}
          borderRadius="2xl"
          boxShadow="0 8px 32px rgba(0, 0, 0, 0.1)"
          overflow="hidden"
          position="relative"
        >
          {/* 상단 그라데이션 배너 */}
          <Box
            h="120px"
            bgGradient="linear(to-r, navy.600, navy.800)"
            position="relative"
            overflow="hidden"
          >
            <Flex
              position="absolute"
              top="0"
              left="0"
              right="0"
              bottom="0"
              align="center"
              justify="center"
            >
              <Heading
                as="h1"
                size="2xl"
                color="white"
                textShadow="0 2px 4px rgba(0,0,0,0.1)"
              >
                everbit
              </Heading>
            </Flex>
          </Box>

          {/* 로그인 폼 영역 */}
          <VStack spacing={8} p={8} align="stretch">
            <VStack spacing={4} align="center">
              <Heading size="lg" color="navy.700">
                환영합니다
              </Heading>
              <Text color="navy.600" textAlign="center">
                비트코인 자동 트레이딩 시스템에 오신 것을 환영합니다.
                <br />
                로그인하여 서비스를 이용해보세요.
              </Text>
            </VStack>

            <Divider />

            <Box
              as="button"
              onClick={handleKakaoLogin}
              cursor="pointer"
              _hover={{ transform: 'translateY(-2px)', transition: 'transform 0.2s' }}
              _active={{ transform: 'translateY(0)' }}
              w="100%"
              h="56px"
            >
              <Image
                src="/images/kakao_login_button.png"
                alt="카카오 로그인"
                w="100%"
                h="100%"
                objectFit="contain"
              />
            </Box>

            <Text
              fontSize="sm"
              color="navy.500"
              textAlign="center"
              mt={4}
            >
              로그인 시 everbit의 이용약관과 개인정보 처리방침에 동의하게 됩니다.
            </Text>
          </VStack>
        </Box>
      </Container>
    </Box>
  );
} 