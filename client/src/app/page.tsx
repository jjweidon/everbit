'use client';

import { Box, Button, Container, Flex, Heading, Text, useColorModeValue, Grid, GridItem, Icon, Badge } from '@chakra-ui/react';
import Image from 'next/image';
import Link from 'next/link';
import { keyframes } from '@emotion/react';
import { FaChartLine, FaRobot, FaHistory, FaBriefcase } from 'react-icons/fa';

const rotateAnimation = keyframes`
  from {
    transform: rotateY(0deg);
  }
  to {
    transform: rotateY(90deg);
  }
`;

export default function Home() {
  // 하늘색 그라데이션 배경
  const skyGradient = 'linear-gradient(to right, #38A4CA, #49C3EC, #B0E7F7)';
  // 금색 그라데이션 배경 (포인트용)
  const goldGradient = 'linear-gradient(to right, #DAA520, #FFC107, #FFD700)';
  
  return (
    <main>
      <Box 
        bgGradient="linear-gradient(to bottom, rgba(229,247,253,0.8), rgba(255,255,255,1))" 
        minH="100vh"
      >
        <Container maxW="container.xl" py={10}>
          <Flex
            direction={{ base: 'column', md: 'row' }}
            align="center"
            justify="space-between"
            gap={8}
            mb={16}
          >
            <Box maxW={{ base: '100%', md: '50%' }}>
              <Heading as="h1" size="2xl" mb={4} bgGradient={skyGradient} bgClip="text">
                everbit
              </Heading>
              <Text fontSize="xl" mb={6} color="gray.700">
                Upbit API를 기반으로 퀀트 전략을 활용하여 최적의 매매 타이밍을 자동으로 판단하고 실행하는 서비스입니다.
              </Text>
              <Flex gap={4} wrap="wrap">
                <Link href="/dashboard" passHref style={{ textDecoration: 'none' }}>
                  <Button 
                    colorScheme="skyblue" 
                    size="lg" 
                    boxShadow="0 4px 8px rgba(73,195,236,0.3)"
                    _hover={{
                      transform: 'translateY(-2px)',
                      boxShadow: '0 6px 12px rgba(73,195,236,0.4)'
                    }}
                  >
                    대시보드 바로가기
                  </Button>
                </Link>
                <Link href="/docs" passHref style={{ textDecoration: 'none' }}>
                  <Button 
                    variant="accent"
                    size="lg" 
                    boxShadow="0 4px 8px rgba(255,193,7,0.3)"
                    _hover={{
                      transform: 'translateY(-2px)',
                      boxShadow: '0 6px 12px rgba(255,193,7,0.4)'
                    }}
                  >
                    시작하기
                  </Button>
                </Link>
              </Flex>
              
              <Box mt={8} display="flex" gap={2}>
                <Badge bg="skyblue.200" px={3} py={1} borderRadius="full" fontSize="sm">
                  실시간 분석
                </Badge>
                <Badge colorScheme="yellow" px={3} py={1} borderRadius="full" fontSize="sm">
                  안전한 자동 트레이딩
                </Badge>
              </Box>
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

          <Heading 
            as="h2" 
            size="xl" 
            mb={10} 
            textAlign="center" 
            bgGradient={skyGradient} 
            bgClip="text"
            position="relative"
            _before={{
              content: '""',
              position: 'absolute',
              bottom: '-10px',
              left: '50%',
              transform: 'translateX(-50%)',
              width: '100px',
              height: '3px',
              background: skyGradient
            }}
          >
            주요 기능
          </Heading>

          <Grid
            templateColumns={{ base: 'repeat(1, 1fr)', md: 'repeat(2, 1fr)' }}
            gap={8}
          >
            {[
              {
                title: '실시간 시세 분석',
                description: '비트코인 실시간 시세를 수집하고 분석하여 트레이딩에 활용합니다.',
                icon: FaChartLine,
                accent: false
              },
              {
                title: '퀀트 알고리즘',
                description: '다양한 퀀트 전략을 기반으로 자동 매매를 실행합니다.',
                icon: FaRobot,
                accent: true
              },
              {
                title: '백테스팅',
                description: '과거 데이터를 활용하여 전략의 성능을 검증합니다.',
                icon: FaHistory,
                accent: true
              },
              {
                title: '포트폴리오 관리',
                description: '자산 배분 및 리스크 관리를 통해 안정적인 수익을 추구합니다.',
                icon: FaBriefcase,
                accent: false
              },
            ].map((feature, index) => (
              <GridItem key={index}>
                <Box
                  bg="white"
                  p={8}
                  borderRadius="lg"
                  boxShadow="md"
                  borderTop="4px solid"
                  borderColor={feature.accent ? "gold.500" : "skyblue.500"}
                  height="100%"
                  transition="all 0.3s"
                  _hover={{
                    transform: 'translateY(-4px)',
                    boxShadow: 'lg',
                    borderColor: feature.accent ? "gold.600" : "skyblue.600",
                  }}
                >
                  <Flex mb={4} align="center">
                    <Box 
                      bg={feature.accent ? "gold.50" : "skyblue.50"} 
                      p={3} 
                      borderRadius="full"
                      mr={4}
                    >
                      <Icon as={feature.icon} color={feature.accent ? "gold.500" : "skyblue.500"} boxSize={6} />
                    </Box>
                    <Heading as="h3" size="md" color={feature.accent ? "gold.600" : "skyblue.700"}>
                      {feature.title}
                    </Heading>
                  </Flex>
                  <Text color="gray.600">{feature.description}</Text>
                </Box>
              </GridItem>
            ))}
          </Grid>
          
          <Box 
            mt={16} 
            p={8} 
            bg="white" 
            borderRadius="lg" 
            boxShadow="md"
            bgGradient="linear-gradient(to right, rgba(176,231,247,0.4), white, rgba(176,231,247,0.2))"
            border="1px solid"
            borderColor="skyblue.200"
          >
            <Flex direction="column" align="center" textAlign="center">
              <Heading size="lg" mb={4} color="skyblue.700">
                비트코인 자동 트레이딩의 미래
              </Heading>
              <Text fontSize="lg" maxW="container.md" mb={6}>
                에버비트와 함께 효율적이고 안전한 암호화폐 트레이딩을 경험해보세요.
              </Text>
              <Flex gap={4}>
                <Link href="/dashboard" passHref style={{ textDecoration: 'none' }}>
                  <Button colorScheme="skyblue" size="lg">
                    시작하기
                  </Button>
                </Link>
                <Link href="/docs" passHref style={{ textDecoration: 'none' }}>
                  <Button variant="accent" size="lg">
                    자세히 알아보기
                  </Button>
                </Link>
              </Flex>
            </Flex>
          </Box>
        </Container>
      </Box>
    </main>
  );
} 