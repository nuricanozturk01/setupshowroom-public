/** @type {import('tailwindcss').Config} */
module.exports = {
  daisyui: {
    base: true,
    styled: true,
    utils: true,
    prefix: "",
    logs: true,
    themeRoot: ":root",
    themes: [
      {
        // Modern Professional Dark Theme
        dark: {
          "color-scheme": "dark",

          // Base colors - Deep navy/slate theme
          "base-100": "#0f172a",     // Main background
          "base-200": "#1e293b",     // Card backgrounds
          "base-300": "#334155",     // Elevated surfaces
          "base-content": "#f8fafc", // Primary text

          // Primary - Clean blue for main actions
          "primary": "#2563eb",
          "primary-content": "#ffffff",

          // Secondary - Neutral slate
          "secondary": "#64748b",
          "secondary-content": "#f1f5f9",

          // Accent - Subtle cyan for highlights
          "accent": "#06b6d4",
          "accent-content": "#ffffff",

          // Neutral - For buttons and interactive elements
          "neutral": "#475569",
          "neutral-content": "#f8fafc",

          // Status colors
          "info": "#0ea5e9",
          "info-content": "#ffffff",
          "success": "#10b981",
          "success-content": "#ffffff",
          "warning": "#f59e0b",
          "warning-content": "#ffffff",
          "error": "#ef4444",
          "error-content": "#ffffff",

          // Border and visual elements
          "--border-color": "#334155",
          "--rounded-box": "0.75rem",
          "--rounded-btn": "0.5rem",
          "--rounded-badge": "1.9rem",
          "--animation-btn": "0.25s",
          "--animation-input": "0.2s",
          "--btn-focus-scale": "0.95",
          "--border-btn": "1px",
          "--tab-border": "1px",
          "--tab-radius": "0.5rem",
        },

        // Light theme variant (optional)
        light: {
          "color-scheme": "light",

          // Base colors - Clean light theme
          "base-100": "#ffffff",
          "base-200": "#f8fafc",
          "base-300": "#e2e8f0",
          "base-content": "#0f172a",

          // Same semantic colors but adjusted for light mode
          "primary": "#2563eb",
          "primary-content": "#ffffff",
          "secondary": "#64748b",
          "secondary-content": "#ffffff",
          "accent": "#06b6d4",
          "accent-content": "#ffffff",
          "neutral": "#475569",
          "neutral-content": "#ffffff",

          "info": "#0ea5e9",
          "info-content": "#ffffff",
          "success": "#10b981",
          "success-content": "#ffffff",
          "warning": "#f59e0b",
          "warning-content": "#ffffff",
          "error": "#ef4444",
          "error-content": "#ffffff",

          "--border-color": "#e2e8f0",
          "--rounded-box": "0.75rem",
          "--rounded-btn": "0.5rem",
          "--rounded-badge": "1.9rem",
          "--animation-btn": "0.25s",
          "--animation-input": "0.2s",
          "--btn-focus-scale": "0.95",
          "--border-btn": "1px",
          "--tab-border": "1px",
          "--tab-radius": "0.5rem",
        },
      },
    ],
  },
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      // Custom colors for specific use cases
      colors: {
        // Setup category colors
        'category': {
          'gaming': '#ef4444',
          'developer': '#3b82f6',
          'productivity': '#10b981',
          'minimalist': '#6b7280',
          'rgb': '#8b5cf6',
          'workspace': '#f59e0b',
        },
        // Interactive states
        'interactive': {
          'hover': 'rgba(255, 255, 255, 0.05)',
          'active': 'rgba(255, 255, 255, 0.1)',
          'focus': 'rgba(37, 99, 235, 0.2)',
        },
        // Glass effect colors
        'glass': {
          'light': 'rgba(255, 255, 255, 0.05)',
          'medium': 'rgba(255, 255, 255, 0.1)',
          'dark': 'rgba(0, 0, 0, 0.2)',
        }
      },

      // Enhanced animations
      keyframes: {
        // Like animation
        'heart-pop': {
          '0%': { transform: 'scale(0.3)', opacity: '0' },
          '30%': { transform: 'scale(1.2)', opacity: '1' },
          '60%': { transform: 'scale(1)', opacity: '1' },
          '100%': { transform: 'scale(1)', opacity: '0' },
        },
        // Slide in animations
        'slide-in-up': {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        'slide-in-right': {
          '0%': { transform: 'translateX(-20px)', opacity: '0' },
          '100%': { transform: 'translateX(0)', opacity: '1' },
        },
        // Pulse effect for loading states
        'pulse-subtle': {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0.7' },
        },
        // Floating effect for cards
        'float': {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-2px)' },
        },
        // Shimmer effect for loading cards
        'shimmer': {
          '0%': { backgroundPosition: '200% 0' },
          '100%': { backgroundPosition: '-200% 0' },
        },
      },
      animation: {
        'heart-pop': 'heart-pop 1s ease-out forwards',
        'slide-in-up': 'slide-in-up 0.3s ease-out',
        'slide-in-right': 'slide-in-right 0.3s ease-out',
        'pulse-subtle': 'pulse-subtle 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'float': 'float 3s ease-in-out infinite',
        'shimmer': 'shimmer 1.5s linear infinite',
      },

      // Typography improvements
      fontWeight: {
        normal: "400",
        medium: "500",
        semibold: "600",
        bold: "700",
        extrabold: "800",
      },
      fontSize: {
        'xs': ['0.75rem', { lineHeight: '1rem' }],
        'sm': ['0.875rem', { lineHeight: '1.25rem' }],
        'base': ['1rem', { lineHeight: '1.5rem' }],
        'lg': ['1.125rem', { lineHeight: '1.75rem' }],
        'xl': ['1.25rem', { lineHeight: '1.75rem' }],
        '2xl': ['1.5rem', { lineHeight: '2rem' }],
        '3xl': ['1.875rem', { lineHeight: '2.25rem' }],
      },
      fontFamily: {
        sans: ["Inter", "system-ui", "-apple-system", "sans-serif"],
        mono: ["JetBrains Mono", "Fira Code", "monospace"],
      },

      // Spacing for consistent layouts
      spacing: {
        '18': '4.5rem',
        '88': '22rem',
        '128': '32rem',
      },

      // Enhanced shadows
      boxShadow: {
        'glow': '0 0 20px rgba(37, 99, 235, 0.3)',
        'glow-lg': '0 0 40px rgba(37, 99, 235, 0.4)',
        'card': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
        'card-hover': '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
        'inner-lg': 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.1)',
      },

      // Backdrop blur utilities
      backdropBlur: {
        'xs': '2px',
        'sm': '4px',
        'md': '8px',
        'lg': '12px',
        'xl': '16px',
      },

      // Border radius consistency
      borderRadius: {
        'none': '0',
        'sm': '0.25rem',
        'DEFAULT': '0.375rem',
        'md': '0.5rem',
        'lg': '0.75rem',
        'xl': '1rem',
        '2xl': '1.5rem',
        '3xl': '2rem',
      },

      // Z-index scale
      zIndex: {
        'dropdown': '1000',
        'sticky': '1020',
        'fixed': '1030',
        'modal-backdrop': '1040',
        'modal': '1050',
        'popover': '1060',
        'tooltip': '1070',
        'toast': '1080',
      },

      // Custom grid templates
      gridTemplateColumns: {
        'setup-grid': 'repeat(auto-fill, minmax(300px, 1fr))',
        'sidebar': '16rem 1fr',
        'profile': '1fr 2fr',
      },

      // Transition timings
      transitionDuration: {
        '0': '0ms',
        '75': '75ms',
        '100': '100ms',
        '150': '150ms',
        '200': '200ms',
        '300': '300ms',
        '500': '500ms',
        '700': '700ms',
        '1000': '1000ms',
      },
    },
  },
  plugins: [
    require('daisyui'),
    // Custom plugin for glass effect utilities
    function({ addUtilities }) {
      const newUtilities = {
        '.glass': {
          'background': 'rgba(255, 255, 255, 0.05)',
          'backdrop-filter': 'blur(10px)',
          'border': '1px solid rgba(255, 255, 255, 0.1)',
        },
        '.glass-card': {
          'background': 'rgba(30, 41, 59, 0.8)',
          'backdrop-filter': 'blur(16px)',
          'border': '1px solid rgba(51, 65, 85, 0.3)',
        },
        '.text-gradient': {
          'background': 'linear-gradient(135deg, #2563eb, #06b6d4)',
          'background-clip': 'text',
          '-webkit-background-clip': 'text',
          '-webkit-text-fill-color': 'transparent',
        },
      }
      addUtilities(newUtilities)
    }
  ],
};
