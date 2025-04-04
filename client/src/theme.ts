'use client';

import { extendTheme } from '@chakra-ui/react';

// 금색 테마 정의
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
    brand: {
      50: '#FFF9E5',
      100: '#FFF0C4',
      200: '#FFE79E',
      300: '#FFDB72',
      400: '#FFD54F',
      500: '#FFC107', // primary 금색
      600: '#DAA520',
      700: '#B8860B',
      800: '#8B6914',
      900: '#5C4611',
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
        colorScheme: 'gold',
      },
    },
  },
});

export default theme; 