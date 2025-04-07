'use client';

import { Box, Container, Heading, Text, Flex, Stat, StatLabel, StatNumber, StatHelpText, Grid, GridItem, Button } from '@chakra-ui/react';
import Link from 'next/link';
import { useState, useEffect } from 'react';
import axios from 'axios';

interface Asset {
  currency: string;
  balance: string;
  locked: string;
  avg_buy_price: string;
  avg_buy_price_modified: boolean;
  unit_currency: string;
}

export default function Dashboard() {
  const skyGradient = 'linear-gradient(to right, #38A4CA, #49C3EC, #B0E7F7)';
  const goldGradient = 'linear-gradient(to right, #DAA520, #FFC107, #FFD700)';
  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalBalance, setTotalBalance] = useState('0');
  const [tradeCount, setTradeCount] = useState(0);
  const [profitRate, setProfitRate] = useState('0');

  useEffect(() => {
    const fetchAssets = async () => {
      try {
        // 스프링 백엔드 서버의 API 엔드포인트 호출
        const response = await axios.get('http://localhost:8080/api/upbit/accounts');
        const assets = response.data;
        setAssets(assets);
        
        // 총 자산 계산 (KRW + 코인 환산 금액)
        let total = 0;
        assets.forEach((asset: Asset) => {
          if (asset.currency === 'KRW') {
            total += parseFloat(asset.balance);
          } else {
            const price = parseFloat(asset.avg_buy_price) * parseFloat(asset.balance);
            total += price;
          }
        });
        
        setTotalBalance(total.toLocaleString());
        setTradeCount(27); // 임시 데이터
        setProfitRate('8.2'); // 임시 데이터
        setLoading(false);
      } catch (err) {
        console.error('자산 정보 가져오기 실패:', err);
        setError('자산 정보를 가져오지 못했습니다. 나중에 다시 시도해주세요.');
        setLoading(false);
      }
    };

    fetchAssets();
  }, []);

  return (
    <Container maxW="container.xl" py={10}>
      <Box mb={8}>
        <Heading as="h1" size="2xl" mb={4} bgGradient={skyGradient} bgClip="text">
          대시보드
        </Heading>
        <Text fontSize="lg" color="gray.700">
          비트코인 자동 트레이딩 현황을 한눈에 확인하세요.
        </Text>
      </Box>
      
      <Grid templateColumns={{ base: 'repeat(1, 1fr)', md: 'repeat(3, 1fr)' }} gap={6} mb={8}>
        <GridItem>
          <Box p={6} bg="white" borderRadius="lg" boxShadow="md" borderTop="4px solid" borderColor="skyblue.500">
            <Stat>
              <StatLabel color="gray.600">현재 잔고</StatLabel>
              <StatNumber color="skyblue.600" fontSize="2xl">{loading ? '로딩 중...' : `₩ ${totalBalance}`}</StatNumber>
              <StatHelpText color="green.500">+12.5%</StatHelpText>
            </Stat>
          </Box>
        </GridItem>
        <GridItem>
          <Box p={6} bg="white" borderRadius="lg" boxShadow="md" borderTop="4px solid" borderColor="skyblue.500">
            <Stat>
              <StatLabel color="gray.600">거래 횟수</StatLabel>
              <StatNumber color="skyblue.600" fontSize="2xl">{loading ? '로딩 중...' : `${tradeCount}회`}</StatNumber>
              <StatHelpText>최근 30일</StatHelpText>
            </Stat>
          </Box>
        </GridItem>
        <GridItem>
          <Box p={6} bg="white" borderRadius="lg" boxShadow="md" borderTop="4px solid" borderColor="gold.500">
            <Stat>
              <StatLabel color="gray.600">수익률</StatLabel>
              <StatNumber color="gold.600" fontSize="2xl">{loading ? '로딩 중...' : `${profitRate}%`}</StatNumber>
              <StatHelpText>연 환산</StatHelpText>
            </Stat>
          </Box>
        </GridItem>
      </Grid>
      
      <Grid templateColumns={{ base: 'repeat(1, 1fr)', md: 'repeat(3, 1fr)' }} gap={6} mb={8}>
        <GridItem colSpan={{ base: 1, md: 2 }}>
          <Box p={8} bg="white" borderRadius="lg" boxShadow="md" borderLeft="4px solid" borderColor="skyblue.500" height="100%">
            <Heading as="h3" size="md" mb={4} color="skyblue.700">트레이딩 현황</Heading>
            {loading ? (
              <Text>로딩 중...</Text>
            ) : error ? (
              <Text color="red.500">{error}</Text>
            ) : (
              <Box maxH="400px" overflowY="auto">
                <Text mb={4}>보유 자산 목록</Text>
                {assets.map((asset, index) => (
                  <Box 
                    key={index} 
                    p={3} 
                    mb={2} 
                    bg={asset.currency === 'KRW' ? 'gray.50' : 'skyblue.50'} 
                    borderRadius="md"
                  >
                    <Flex justifyContent="space-between">
                      <Text fontWeight="bold">{asset.currency}</Text>
                      <Text>{parseFloat(asset.balance).toLocaleString()} {asset.currency}</Text>
                    </Flex>
                    {asset.currency !== 'KRW' && (
                      <Flex justifyContent="space-between" mt={1}>
                        <Text fontSize="sm">평균 매수가</Text>
                        <Text fontSize="sm">{parseFloat(asset.avg_buy_price).toLocaleString()} KRW</Text>
                      </Flex>
                    )}
                  </Box>
                ))}
              </Box>
            )}
          </Box>
        </GridItem>
        <GridItem>
          <Box p={8} bg="white" borderRadius="lg" boxShadow="md" borderLeft="4px solid" borderColor="gold.500" height="100%">
            <Heading as="h3" size="md" mb={4} color="gold.600">시장 정보</Heading>
            <Text mb={4}>최신 시장 동향과 정보를 확인하세요.</Text>
            <Box bg="gold.50" p={4} borderRadius="md">
              <Text fontWeight="bold" color="gold.600">비트코인 가격:</Text>
              <Text fontSize="xl">₩ 86,420,000</Text>
            </Box>
          </Box>
        </GridItem>
      </Grid>
      
      <Box borderRadius="lg" overflow="hidden" mb={8}>
        <Flex>
          <Box
            bg="skyblue.500"
            p={4}
            width="50%"
            textAlign="center"
            fontWeight="bold"
            color="white"
          >
            자동 매매
          </Box>
          <Box
            bg="gold.400"
            p={4}
            width="50%"
            textAlign="center"
            fontWeight="bold"
            color="white"
          >
            수동 매매
          </Box>
        </Flex>
        <Box p={8} bg="white" borderRadius="0 0 lg lg" boxShadow="md" border="1px solid" borderColor="gray.200" borderTop="none">
          <Text fontSize="lg" mb={4}>트레이딩 설정을 구성하세요.</Text>
          <Flex gap={4}>
            <Button colorScheme="skyblue">설정 저장</Button>
            <Button variant="accent" onClick={() => window.location.reload()}>새로고침</Button>
          </Flex>
        </Box>
      </Box>
    </Container>
  );
} 