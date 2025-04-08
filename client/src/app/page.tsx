'use client';

import { Box, Container, Heading, Text, Flex, Button, Badge, Grid, GridItem, Icon } from '@chakra-ui/react';
import { FaChartLine, FaRobot, FaHistory, FaBriefcase } from 'react-icons/fa';
import Link from 'next/link';
import Image from 'next/image';

export default function Home() {
  return (
    <main>
      <Box bg="white" minH="100vh">
        <Container maxW="container.xl" py={10}>
          <Flex
            direction={{ base: 'column', md: 'row' }}
            align="center"
            justify="space-between"
            gap={8}
            mb={16}
          >
            <Box maxW={{ base: '100%', md: '50%' }}>
              <Heading as="h1" size="2xl" mb={4} color="navy.700">
                everbit
              </Heading>
              <Text fontSize="xl" mb={6} color="navy.600">
                Upbit API를 기반으로 퀀트 전략을 활용하여 최적의 매매 타이밍을 자동으로 판단하고 실행하는 서비스입니다.
              </Text>
              <Flex gap={4} wrap="wrap">
                <Link href="/dashboard" passHref style={{ textDecoration: 'none' }}>
                  <Button 
                    colorScheme="navy" 
                    size="lg" 
                    boxShadow="0 4px 8px rgba(41,62,125,0.3)"
                    _hover={{
                      transform: 'translateY(-2px)',
                      boxShadow: '0 6px 12px rgba(41,62,125,0.4)'
                    }}
                  >
                    대시보드 바로가기
                  </Button>
                </Link>
                <Link href="/docs" passHref style={{ textDecoration: 'none' }}>
                  <Button 
                    variant="accent"
                    size="lg" 
                    boxShadow="0 4px 8px rgba(41,62,125,0.3)"
                    _hover={{
                      transform: 'translateY(-2px)',
                      boxShadow: '0 6px 12px rgba(41,62,125,0.4)'
                    }}
                  >
                    시작하기
                  </Button>
                </Link>
              </Flex>
              
              <Box mt={8} display="flex" gap={2}>
                <Badge bg="navy.100" color="navy.700" px={3} py={1} borderRadius="full" fontSize="sm">
                  실시간 분석
                </Badge>
                <Badge bg="navy.100" color="navy.700" px={3} py={1} borderRadius="full" fontSize="sm">
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
            color="navy.700"
            position="relative"
            _before={{
              content: '""',
              position: 'absolute',
              bottom: '-10px',
              left: '50%',
              transform: 'translateX(-50%)',
              width: '100px',
              height: '3px',
              background: 'navy.500'
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
            ].map((item, index) => (
              <GridItem key={index}>
                <Box 
                  p={8} 
                  bg="white" 
                  borderRadius="lg" 
                  boxShadow="md"
                  borderLeft="4px solid"
                  borderColor={item.accent ? "navy.500" : "navy.400"}
                  height="100%"
                >
                  <Flex align="center" mb={4}>
                    <Icon as={item.icon} color="navy.500" boxSize={6} mr={3} />
                    <Heading as="h3" size="md" color="navy.700">
                      {item.title}
                    </Heading>
                  </Flex>
                  <Text color="navy.600">{item.description}</Text>
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
            bgGradient="linear-gradient(to right, rgba(230,232,240,0.4), white, rgba(230,232,240,0.2))"
            border="1px solid"
            borderColor="navy.200"
          >
            <Flex direction="column" align="center" textAlign="center">
              <Heading size="lg" mb={4} color="navy.700">
                비트코인 자동 트레이딩의 미래
              </Heading>
              <Text fontSize="lg" maxW="container.md" mb={6} color="navy.600">
                에버비트와 함께 효율적이고 안전한 암호화폐 트레이딩을 경험해보세요.
              </Text>
              <Flex gap={4}>
                <Link href="/dashboard" passHref style={{ textDecoration: 'none' }}>
                  <Button colorScheme="navy" size="lg">
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