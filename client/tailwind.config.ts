import type { Config } from "tailwindcss";

export default {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      borderWidth: {
        thin: "0.5px",
      },
      borderRadius: {
        "token-lg": "var(--radius-lg)",
        "token-md": "var(--radius-md)",
      },
      colors: {
        bg0: "rgb(var(--bg-0) / <alpha-value>)",
        bg1: "rgb(var(--bg-1) / <alpha-value>)",
        bg2: "rgb(var(--bg-2) / <alpha-value>)",
        border: "rgb(var(--border) / <alpha-value>)",
        /* 레이아웃 보더: 어두운 톤으로 직접 지정해 실제 스타일에 반영 */
        borderSubtle: "rgb(24 27 32 / <alpha-value>)",
        divider: "rgb(var(--divider) / <alpha-value>)",
        "text-1": "rgb(var(--text-1) / <alpha-value>)",
        "text-heading": "rgb(var(--text-heading) / <alpha-value>)",
        "text-2": "rgb(var(--text-2) / <alpha-value>)",
        "text-3": "rgb(var(--text-3) / <alpha-value>)",
        green: "rgb(var(--green) / <alpha-value>)",
        red: "rgb(var(--red) / <alpha-value>)",
        yellow: "rgb(var(--yellow) / <alpha-value>)",
        cyan: "rgb(var(--cyan) / <alpha-value>)",
      },
    },
  },
  plugins: [],
} satisfies Config;
