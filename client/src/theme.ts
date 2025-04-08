'use client';

import { extendTheme } from '@chakra-ui/react';

// 하늘색을 메인 테마로 설정
const theme = extendTheme({
  colors: {
    navy: {
      50: '#E6E8F0',
      100: '#C1C6D9',
      200: '#9BA4C2',
      300: '#7582AB',
      400: '#4F6094',
      500: '#293E7D', // primary
      600: '#213264',
      700: '#19254B',
      800: '#111932',
      900: '#090C19',
    },
    // 하늘색 팔레트 (메인 색상)
    skyblue: {
      50: '#E5F7FD',
      100: '#D0EFFA',
      200: '#B0E7F7', 
      300: '#8EDCF4',
      400: '#6BD0F0',
      500: '#49C3EC', // primary
      600: '#38A4CA',
      700: '#2886A9',
      800: '#196887',
      900: '#0B4B66',
    },
    // 브랜드 색상을 하늘색으로 변경
    brand: {
      50: '#E6E8F0',
      100: '#C1C6D9',
      200: '#9BA4C2',
      300: '#7582AB',
      400: '#4F6094',
      500: '#293E7D', // primary navy
      600: '#213264',
      700: '#19254B',
      800: '#111932',
      900: '#090C19',
    },
    // 메인과 포인트 색상 정의
    accent: {
      light: '#E6E8F0', // 밝은 네이비
      main: '#293E7D', // 네이비
      dark: '#19254B', // 어두운 네이비
    },
  },
  styles: {
    global: {
      body: {
        bg: 'white',
        color: 'navy.800',
      },
    },
  },
  components: {
    Button: {
      defaultProps: {
        colorScheme: 'navy',
      },
      variants: {
        accent: {
          bg: 'navy.500',
          color: 'white',
          _hover: {
            bg: 'navy.600',
          },
        },
      },
    },
  },
});

export default theme; 