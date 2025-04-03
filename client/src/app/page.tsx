'use client';

import { Box, Button, Container, Flex, Heading, Text } from '@chakra-ui/react';
import Image from 'next/image';
import Link from 'next/link';
import { keyframes } from '@emotion/react';

const rotateAnimation = keyframes`
  from {
    transform: rotateY(0deg);
  }
  to {
    transform: rotateY(90deg);
  }
`;

export default function Home() {
  return (
    <main>
      <Container maxW="container.xl" py={10}>
        <Flex
          direction={{ base: 'column', md: 'row' }}
          align="center"
          justify="space-between"
          gap={8}
          mb={16}
        >
          <Box maxW={{ base: '100%', md: '50%' }}>
            <Heading as="h1" size="2xl" mb={4}>
              everbit
            </Heading>
            <Text fontSize="xl" mb={6} color="gray.600">
              Upbit API를 기반으로 퀀트 전략을 활용하여 최적의 매매 타이밍을 자동으로 판단하고 실행하는 서비스입니다.
            </Text>
            <Flex gap={4}>
              <Link href="/dashboard" passHref style={{ textDecoration: 'none' }}>
                <Button colorScheme="blue" size="lg">
                  대시보드 바로가기
                </Button>
              </Link>
              <Link href="/docs" passHref style={{ textDecoration: 'none' }}>
                <Button colorScheme="gray" size="lg" variant="outline">
                  시작하기
                </Button>
              </Link>
            </Flex>
          </Box>
          <Box
            position="relative"
            width={{ base: '100%', md: '50%' }}
            height={{ base: '300px', md: '400px' }}
            borderRadius="xl"
            overflow="hidden"
            bg="none"
            sx={{
              transformStyle: 'preserve-3d',
              animation: `${rotateAnimation} 2s ease-in-out infinite alternate`,
              '&:hover': {
                animation: `${rotateAnimation} 1s ease-in-out infinite alternate`,
              },
            }}
          >
            <Image
              src="/images/logo-image.png"
              alt="everbit 로고"
              fill
              style={{ objectFit: 'contain' }}
              priority
            />
          </Box>
        </Flex>

        <Heading as="h2" size="xl" mb={10} textAlign="center">
          주요 기능
        </Heading>

        <Flex
          wrap="wrap"
          justify="space-between"
          gap={6}
        >
          {[
            {
              title: '실시간 시세 분석',
              description: '비트코인 실시간 시세를 수집하고 분석하여 트레이딩에 활용합니다.',
              icon: '📊',
            },
            {
              title: '퀀트 알고리즘',
              description: '다양한 퀀트 전략을 기반으로 자동 매매를 실행합니다.',
              icon: '🤖',
            },
            {
              title: '백테스팅',
              description: '과거 데이터를 활용하여 전략의 성능을 검증합니다.',
              icon: '📈',
            },
            {
              title: '포트폴리오 관리',
              description: '자산 배분 및 리스크 관리를 통해 안정적인 수익을 추구합니다.',
              icon: '💼',
            },
          ].map((feature, index) => (
            <Box
              key={index}
              flex={{ base: '1 1 100%', md: '1 1 45%' }}
              bg="white"
              p={6}
              borderRadius="lg"
              boxShadow="md"
            >
              <Text fontSize="3xl" mb={3}>
                {feature.icon}
              </Text>
              <Heading as="h3" size="md" mb={2}>
                {feature.title}
              </Heading>
              <Text color="gray.600">{feature.description}</Text>
            </Box>
          ))}
        </Flex>
      </Container>
    </main>
  );
} 