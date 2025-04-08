'use client';

import { Box, Container, Heading, Text, Flex, Stat, StatLabel, StatNumber, StatHelpText, Grid, GridItem, Button } from '@chakra-ui/react';
import Link from 'next/link';
import { useState, useEffect } from 'react';
import { UpbitAccount, AccountSummary } from '@/types/upbit';
import { upbitApi } from '@/api/upbit';

export default function Dashboard() {
  const [accountSummary, setAccountSummary] = useState<AccountSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    const fetchAccounts = async () => {
      try {
        const accounts = await upbitApi.getAccounts();
        
        if (!mounted) return;

        const summary: AccountSummary = {
          totalBalance: 0,
          totalProfit: 0,
          profitRate: 0,
          accounts: accounts
        };

        summary.totalBalance = accounts.reduce((total, account) => {
          const balance = parseFloat(account.balance) + parseFloat(account.locked);
          if (account.currency === 'KRW') {
            return total + balance;
          }
          const avgPrice = parseFloat(account.avg_buy_price);
          return total + (balance * avgPrice);
        }, 0);

        setAccountSummary(summary);
      } catch (err) {
        if (!mounted) return;
        setError('계좌 정보를 불러오는데 실패했습니다.');
        console.error('Failed to fetch accounts:', err);
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };

    fetchAccounts();

    return () => {
      mounted = false;
    };
  }, []);

  if (loading) {
    return (
      <Container maxW="container.xl" py={10}>
        <Box textAlign="center" py={20}>
          <Text fontSize="xl" color="navy.600">로딩 중...</Text>
        </Box>
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxW="container.xl" py={10}>
        <Box textAlign="center" py={20}>
          <Text fontSize="xl" color="red.500">{error}</Text>
          <Button mt={4} colorScheme="navy" onClick={() => window.location.reload()}>
            다시 시도
          </Button>
        </Box>
      </Container>
    );
  }

  if (!accountSummary) {
    return (
      <Container maxW="container.xl" py={10}>
        <Box textAlign="center" py={20}>
          <Text fontSize="xl" color="navy.600">데이터가 없습니다.</Text>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxW="container.xl" py={10}>
      <Box mb={8}>
        <Heading as="h1" size="2xl" mb={4} color="navy.700">
          대시보드
        </Heading>
        <Text fontSize="lg" color="navy.600">
          비트코인 자동 트레이딩 현황을 한눈에 확인하세요.
        </Text>
      </Box>
      
      <Grid templateColumns={{ base: 'repeat(1, 1fr)', md: 'repeat(3, 1fr)' }} gap={6} mb={8}>
        <GridItem>
          <Box p={6} bg="white" borderRadius="lg" boxShadow="md" borderTop="4px solid" borderColor="navy.500">
            <Stat>
              <StatLabel color="navy.600">현재 잔고</StatLabel>
              <StatNumber color="navy.700" fontSize="2xl">{accountSummary.totalBalance.toLocaleString()} KRW</StatNumber>
            </Stat>
          </Box>
        </GridItem>
        <GridItem>
          <Box p={6} bg="white" borderRadius="lg" boxShadow="md" borderTop="4px solid" borderColor="navy.400">
            <Stat>
              <StatLabel color="navy.600">수익률</StatLabel>
              <StatNumber color="navy.700" fontSize="2xl">{accountSummary.profitRate}%</StatNumber>
            </Stat>
          </Box>
        </GridItem>
      </Grid>
      
      <Grid templateColumns={{ base: 'repeat(1, 1fr)', md: 'repeat(3, 1fr)' }} gap={6} mb={8}>
        <GridItem colSpan={{ base: 1, md: 2 }}>
          <Box p={8} bg="white" borderRadius="lg" boxShadow="md" borderLeft="4px solid" borderColor="navy.500" height="100%">
            <Heading as="h3" size="md" mb={4} color="navy.700">트레이딩 현황</Heading>
            <Text mb={4} color="navy.600">보유 자산 목록</Text>
            <div className="bg-white rounded-lg shadow overflow-hidden">
              <table className="min-w-full">
                <thead className="bg-navy-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-navy-600 uppercase tracking-wider">화폐</th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-navy-600 uppercase tracking-wider">보유수량</th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-navy-600 uppercase tracking-wider">주문중</th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-navy-600 uppercase tracking-wider">매수평균가</th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-navy-600 uppercase tracking-wider">평가금액</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-navy-100">
                  {accountSummary.accounts.map((account) => {
                    const balance = parseFloat(account.balance);
                    const locked = parseFloat(account.locked);
                    const avgPrice = parseFloat(account.avg_buy_price);
                    const totalValue = (balance + locked) * (account.currency === 'KRW' ? 1 : avgPrice);

                    return (
                      <tr key={account.currency}>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="font-medium text-navy-900">{account.currency}</div>
                        </td>
                        <td className="px-6 py-4 text-right whitespace-nowrap">
                          {parseFloat(account.balance).toLocaleString(undefined, { maximumFractionDigits: 8 })}
                        </td>
                        <td className="px-6 py-4 text-right whitespace-nowrap">
                          {parseFloat(account.locked).toLocaleString(undefined, { maximumFractionDigits: 8 })}
                        </td>
                        <td className="px-6 py-4 text-right whitespace-nowrap">
                          {account.currency === 'KRW' ? '-' : 
                            parseFloat(account.avg_buy_price).toLocaleString() + ' ' + account.unit_currency}
                        </td>
                        <td className="px-6 py-4 text-right whitespace-nowrap">
                          {totalValue.toLocaleString()} {account.unit_currency}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </Box>
        </GridItem>
        <GridItem>
          <Box p={8} bg="white" borderRadius="lg" boxShadow="md" borderLeft="4px solid" borderColor="navy.400" height="100%">
            <Heading as="h3" size="md" mb={4} color="navy.700">시장 정보</Heading>
            <Text mb={4} color="navy.600">최신 시장 동향과 정보를 확인하세요.</Text>
            <Box bg="navy.50" p={4} borderRadius="md">
              <Text fontWeight="bold" color="navy.700">비트코인 가격:</Text>
              <Text fontSize="xl" color="navy.800">₩ 86,420,000</Text>
            </Box>
          </Box>
        </GridItem>
      </Grid>
      
      <Box borderRadius="lg" overflow="hidden" mb={8}>
        <Flex>
          <Box
            bg="navy.500"
            p={4}
            width="50%"
            textAlign="center"
            fontWeight="bold"
            color="white"
          >
            자동 매매
          </Box>
          <Box
            bg="navy.400"
            p={4}
            width="50%"
            textAlign="center"
            fontWeight="bold"
            color="white"
          >
            수동 매매
          </Box>
        </Flex>
        <Box p={8} bg="white" borderRadius="0 0 lg lg" boxShadow="md" border="1px solid" borderColor="navy.200" borderTop="none">
          <Text fontSize="lg" mb={4} color="navy.700">트레이딩 설정을 구성하세요.</Text>
          <Flex gap={4}>
            <Button colorScheme="navy">설정 저장</Button>
            <Button variant="accent" onClick={() => window.location.reload()}>새로고침</Button>
          </Flex>
        </Box>
      </Box>
    </Container>
  );
} 