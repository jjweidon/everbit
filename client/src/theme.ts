'use client';

import { extendTheme } from '@chakra-ui/react';

// 하늘색을 메인 테마로 설정
const theme = extendTheme({
  colors: {
    gold: {
      50: '#FFF9E5',
      100: '#FFF0C4',
      200: '#FFE79E',
      300: '#FFDB72',
      400: '#FFD54F',
      500: '#FFC107', // primary
      600: '#DAA520', // darkgold
      700: '#B8860B', // goldenrod
      800: '#8B6914',
      900: '#5C4611',
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
      50: '#E5F7FD',
      100: '#D0EFFA',
      200: '#B0E7F7',
      300: '#8EDCF4',
      400: '#6BD0F0',
      500: '#49C3EC', // primary 하늘색
      600: '#38A4CA',
      700: '#2886A9',
      800: '#196887',
      900: '#0B4B66',
    },
    // 메인과 포인트 색상 정의
    accent: {
      light: '#FFE79E', // 밝은 금색
      main: '#FFC107', // 금색
      dark: '#38A4CA', // 어두운 하늘색
    },
  },
  styles: {
    global: {
      body: {
        bg: 'white',
        color: 'gray.800',
      },
    },
  },
  components: {
    Button: {
      defaultProps: {
        colorScheme: 'skyblue',
      },
      variants: {
        accent: {
          bg: 'accent.main',
          color: 'gray.800',
          _hover: {
            bg: 'gold.400',
          },
        },
      },
    },
  },
});

export default theme; 