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
        navy: {
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
        brand: {
          50: '#E6E8F0',
          100: '#C1C6D9',
          200: '#9BA4C2',
          300: '#7582AB',
          400: '#4F6094',
          500: '#293E7D',
          600: '#213264',
          700: '#19254B',
          800: '#111932',
          900: '#090C19',
        },
        accent: {
          light: '#E6E8F0',
          main: '#293E7D',
          dark: '#19254B',
        },
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'gradient-conic':
          'conic-gradient(from 180deg at 50% 50%, var(--tw-gradient-stops))',
      },
      fontFamily: {
        sans: ['var(--font-noto-sans-kr)'],
        logo: ['var(--font-logo)'],
        kimm: ['var(--font-kimm)'],
      },
      animation: {
        'fade-in': 'fadeIn 1s ease-in-out',
        'fade-in-up': 'fadeInUp 1s ease-in-out',
        'fade-in-down': 'fadeInDown 1s ease-in-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        fadeInUp: {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        fadeInDown: {
          '0%': { opacity: '0', transform: 'translateY(-20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
    },
  },
  plugins: [],
}; 