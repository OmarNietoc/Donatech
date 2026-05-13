import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#1565c0',
          dark: '#003c8f',
          light: '#4f83cc',
        },
        danger: '#c62828',
        success: '#2e7d32',
      },
    },
  },
  plugins: [],
} satisfies Config
