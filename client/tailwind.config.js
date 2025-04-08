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
        primary: {
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