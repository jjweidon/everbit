'use client';

import { Box, Container, Heading, Text, List, ListItem, ListIcon, Divider, Flex, Button, Grid, GridItem, Icon } from '@chakra-ui/react';
import { MdCheckCircle, MdArrowForward, MdInfo, MdQuestionAnswer, MdSchool } from 'react-icons/md';
import Link from 'next/link';

export default function Docs() {
  const skyGradient = 'linear-gradient(to right, #38A4CA, #49C3EC, #B0E7F7)';
  const goldGradient = 'linear-gradient(to right, #DAA520, #FFC107, #FFD700)';

  return (
    <Container maxW="container.xl" py={10}>
      <Box mb={8}>
        <Heading as="h1" size="2xl" mb={4} bgGradient={skyGradient} bgClip="text">
          시작하기
        </Heading>
        <Text fontSize="lg" color="gray.700">
          에버비트 사용 방법을 안내합니다.
        </Text>
      </Box>
      
      <Grid templateColumns={{ base: 'repeat(1, 1fr)', md: 'repeat(3, 1fr)' }} gap={6} mb={8}>
        <GridItem colSpan={{ base: 1, md: 2 }}>
          <Box p={8} bg="white" borderRadius="lg" boxShadow="md" mb={6} borderLeft="4px solid" borderColor="skyblue.500">
            <Heading as="h2" size="lg" mb={4} color="skyblue.700">
              주요 기능 가이드
            </Heading>
            
            <List spacing={3} my={4}>
              <ListItem>
                <ListIcon as={MdCheckCircle} color="skyblue.500" />
                실시간 비트코인 시세 조회
              </ListItem>
              <ListItem>
                <ListIcon as={MdCheckCircle} color="skyblue.500" />
                자동 트레이딩 설정 방법
              </ListItem>
              <ListItem>
                <ListIcon as={MdCheckCircle} color="skyblue.500" />
                백테스팅 기능 활용법
              </ListItem>
              <ListItem>
                <ListIcon as={MdCheckCircle} color="skyblue.500" />
                포트폴리오 관리 및 분석
              </ListItem>
            </List>
            
            <Divider my={6} borderColor="skyblue.200" />
            
            <Text mb={4}>
              상세 문서는 준비 중입니다. 빠른 시일 내에 제공하겠습니다.
            </Text>
            
            <Flex justify="flex-end">
              <Link href="/dashboard" passHref style={{ textDecoration: 'none' }}>
                <Button colorScheme="skyblue" rightIcon={<MdArrowForward />}>
                  대시보드로 이동
                </Button>
              </Link>
            </Flex>
          </Box>
        </GridItem>

        <GridItem>
          <Box p={8} bg="white" borderRadius="lg" boxShadow="md" height="100%" borderLeft="4px solid" borderColor="gold.500">
            <Heading as="h2" size="md" mb={4} color="gold.600">
              자주 묻는 질문
            </Heading>
            
            <List spacing={4}>
              {[
                { icon: MdInfo, title: '서비스 개요', description: '에버비트는 비트코인 자동 트레이딩 서비스입니다.' },
                { icon: MdQuestionAnswer, title: '트레이딩 방식', description: '퀀트 전략을 활용하여 매매 신호를 생성합니다.' },
                { icon: MdSchool, title: '사용 방법', description: '대시보드에서 간편하게 설정할 수 있습니다.' },
              ].map((item, index) => (
                <ListItem key={index} p={3} mb={2} bg="gold.50" borderRadius="md">
                  <Flex align="center">
                    <Icon as={item.icon} color="gold.500" mr={2} />
                    <Box>
                      <Text fontWeight="bold">{item.title}</Text>
                      <Text fontSize="sm" color="gray.600">{item.description}</Text>
                    </Box>
                  </Flex>
                </ListItem>
              ))}
            </List>
            
            <Flex justify="center" mt={6}>
              <Button variant="accent" size="md">
                문의하기
              </Button>
            </Flex>
          </Box>
        </GridItem>
      </Grid>
      
      <Box p={6} bg="white" borderRadius="lg" boxShadow="md" bgGradient="linear-gradient(to right, rgba(176,231,247,0.4), white, rgba(176,231,247,0.3))">
        <Flex align="center" justify="space-between" wrap="wrap" gap={4}>
          <Text fontWeight="bold" fontSize="lg">더 자세한 내용이 필요하신가요?</Text>
          <Flex gap={4}>
            <Button colorScheme="skyblue">튜토리얼 보기</Button>
            <Button variant="accent">API 문서</Button>
          </Flex>
        </Flex>
      </Box>
    </Container>
  );
} 