'use client';

import { Box, Container, Heading, Text } from '@chakra-ui/react';

export default function Dashboard() {
  return (
    <Container maxW="container.xl" py={10}>
      <Box mb={8}>
        <Heading as="h1" size="2xl" mb={4}>
          대시보드
        </Heading>
        <Text fontSize="lg" color="gray.600">
          비트코인 자동 트레이딩 현황을 한눈에 확인하세요.
        </Text>
      </Box>
      
      <Box p={8} bg="white" borderRadius="lg" boxShadow="md">
        <Text fontSize="xl">트레이딩 대시보드가 준비 중입니다.</Text>
      </Box>
    </Container>
  );
} 