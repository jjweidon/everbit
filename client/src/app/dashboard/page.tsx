'use client';

import { Box, Container, Heading, Text, Flex, Stat, StatLabel, StatNumber, StatHelpText, Grid, GridItem } from '@chakra-ui/react';

export default function Dashboard() {
  const goldGradient = 'linear-gradient(to right, #DAA520, #FFC107, #FFD700)';

  return (
    <Container maxW="container.xl" py={10}>
      <Box mb={8}>
        <Heading as="h1" size="2xl" mb={4} bgGradient={goldGradient} bgClip="text">
          대시보드
        </Heading>
        <Text fontSize="lg" color="gray.700">
          비트코인 자동 트레이딩 현황을 한눈에 확인하세요.
        </Text>
      </Box>
      
      <Grid templateColumns={{ base: 'repeat(1, 1fr)', md: 'repeat(3, 1fr)' }} gap={6} mb={8}>
        <GridItem>
          <Box p={6} bg="white" borderRadius="lg" boxShadow="md" borderTop="4px solid" borderColor="gold.500">
            <Stat>
              <StatLabel color="gray.600">현재 잔고</StatLabel>
              <StatNumber color="gold.600" fontSize="2xl">₩ 1,250,000</StatNumber>
              <StatHelpText color="green.500">+12.5%</StatHelpText>
            </Stat>
          </Box>
        </GridItem>
        <GridItem>
          <Box p={6} bg="white" borderRadius="lg" boxShadow="md" borderTop="4px solid" borderColor="gold.500">
            <Stat>
              <StatLabel color="gray.600">거래 횟수</StatLabel>
              <StatNumber color="gold.600" fontSize="2xl">27회</StatNumber>
              <StatHelpText>최근 30일</StatHelpText>
            </Stat>
          </Box>
        </GridItem>
        <GridItem>
          <Box p={6} bg="white" borderRadius="lg" boxShadow="md" borderTop="4px solid" borderColor="gold.500">
            <Stat>
              <StatLabel color="gray.600">수익률</StatLabel>
              <StatNumber color="gold.600" fontSize="2xl">8.2%</StatNumber>
              <StatHelpText>연 환산</StatHelpText>
            </Stat>
          </Box>
        </GridItem>
      </Grid>
      
      <Box p={8} bg="white" borderRadius="lg" boxShadow="md" borderLeft="4px solid" borderColor="gold.500">
        <Heading as="h3" size="md" mb={4} color="gold.700">진행 상황</Heading>
        <Text fontSize="xl">트레이딩 대시보드가 준비 중입니다.</Text>
      </Box>
    </Container>
  );
} 