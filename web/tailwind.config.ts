import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./src/**/*.{js,ts,jsx,tsx,mdx}"],
  theme: {
    extend: {
      colors: {
        // Aurora palette — values mirror /design/tokens/colors.json
        bg: "#0A0612",
        "bg-2": "#120A1F",
        surface: "#1A0F2E",
        "surface-2": "#24173D",
        purple: {
          DEFAULT: "#9C5BFF",
          bright: "#B47BFF",
          deep: "#6B2FD9",
        },
        pink: "#EC4899",
        amber: "#F59E0B",
        blue: "#3B82F6",
        moonlight: "#F5F3FF",
        success: "#10B981",
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
        serif: ["Instrument Serif", "Georgia", "serif"],
        mono: ["JetBrains Mono", "ui-monospace", "monospace"],
      },
      backgroundImage: {
        "aurora-gradient":
          "linear-gradient(135deg, #9C5BFF 0%, #EC4899 50%, #F59E0B 100%)",
        "glow-gradient":
          "linear-gradient(135deg, #9C5BFF 0%, #3B82F6 100%)",
      },
      animation: {
        "aurora-drift": "aurora-drift 20s ease-in-out infinite",
        "fade-up": "fade-up 0.6s ease-out forwards",
      },
      keyframes: {
        "aurora-drift": {
          "0%, 100%": { transform: "translate(0, 0) scale(1)" },
          "50%": { transform: "translate(30px, -30px) scale(1.1)" },
        },
        "fade-up": {
          "0%": { opacity: "0", transform: "translateY(20px)" },
          "100%": { opacity: "1", transform: "translateY(0)" },
        },
      },
    },
  },
  plugins: [],
};

export default config;
