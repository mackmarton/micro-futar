import type { Config } from 'tailwindcss';
import sharedPreset from '@package/shared-ui/tailwind-preset';

const config: Config = {
  presets: [sharedPreset],
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      // Keycloak theme keeps its smaller radii (differs from the shared preset).
      borderRadius: {
        lg: '0.5rem',
        xl: '0.75rem',
      },
    },
  },
  plugins: [],
};

export default config;
