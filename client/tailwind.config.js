/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#FFF9E5',
          100: '#FFF0C4',
          200: '#FFE79E',
          300: '#FFDB72',
          400: '#FFD54F',
          500: '#FFC107', // primary gold
          600: '#DAA520', // darkgold
          700: '#B8860B', // goldenrod
          800: '#8B6914',
          900: '#5C4611',
          950: '#3A2A0B',
        },
        gold: {
          50: '#FFF9E5',
          100: '#FFF0C4',
          200: '#FFE79E',
          300: '#FFDB72',
          400: '#FFD54F',
          500: '#FFC107', // primary gold
          600: '#DAA520', // darkgold
          700: '#B8860B', // goldenrod
          800: '#8B6914',
          900: '#5C4611',
          950: '#3A2A0B',
        },
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'gradient-conic':
          'conic-gradient(from 180deg at 50% 50%, var(--tw-gradient-stops))',
      },
    },
  },
  plugins: [],
}; 