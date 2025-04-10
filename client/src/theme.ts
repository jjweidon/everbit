'use client';

import { extendTheme } from '@chakra-ui/react';

// 하늘색을 메인 테마로 설정
const theme = extendTheme({
  fonts: {
    heading: 'var(--font-noto-sans-kr)',
    body: 'var(--font-noto-sans-kr)',
  },
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
        fontFamily: 'var(--font-noto-sans-kr)',
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