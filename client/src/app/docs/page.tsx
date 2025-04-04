'use client';

import { Box, Container, Heading, Text, List, ListItem, ListIcon, Divider, Flex, Button } from '@chakra-ui/react';
import { MdCheckCircle, MdArrowForward } from 'react-icons/md';
import Link from 'next/link';

export default function Docs() {
  const goldGradient = 'linear-gradient(to right, #DAA520, #FFC107, #FFD700)';

  return (
    <Container maxW="container.xl" py={10}>
      <Box mb={8}>
        <Heading as="h1" size="2xl" mb={4} bgGradient={goldGradient} bgClip="text">
          시작하기
        </Heading>
        <Text fontSize="lg" color="gray.700">
          에버비트 사용 방법을 안내합니다.
        </Text>
      </Box>
      
      <Box p={8} bg="white" borderRadius="lg" boxShadow="md" mb={6} borderLeft="4px solid" borderColor="gold.500">
        <Heading as="h2" size="lg" mb={4} color="gold.700">
          주요 기능 가이드
        </Heading>
        
        <List spacing={3} my={4}>
          <ListItem>
            <ListIcon as={MdCheckCircle} color="gold.500" />
            실시간 비트코인 시세 조회
          </ListItem>
          <ListItem>
            <ListIcon as={MdCheckCircle} color="gold.500" />
            자동 트레이딩 설정 방법
          </ListItem>
          <ListItem>
            <ListIcon as={MdCheckCircle} color="gold.500" />
            백테스팅 기능 활용법
          </ListItem>
          <ListItem>
            <ListIcon as={MdCheckCircle} color="gold.500" />
            포트폴리오 관리 및 분석
          </ListItem>
        </List>
        
        <Divider my={6} borderColor="gold.200" />
        
        <Text mb={4}>
          상세 문서는 준비 중입니다. 빠른 시일 내에 제공하겠습니다.
        </Text>
        
        <Flex justify="flex-end">
          <Link href="/dashboard" passHref style={{ textDecoration: 'none' }}>
            <Button colorScheme="gold" rightIcon={<MdArrowForward />}>
              대시보드로 이동
            </Button>
          </Link>
        </Flex>
      </Box>
    </Container>
  );
} 