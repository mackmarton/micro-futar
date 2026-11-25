import type { Config } from 'tailwindcss';
import forms from '@tailwindcss/forms';
import containerQueries from '@tailwindcss/container-queries';
import sharedPreset from '@package/shared-ui/tailwind-preset';

const config: Config = {
  presets: [sharedPreset],
  darkMode: 'class',
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
    '../../packages/shared-ui/**/*.{js,ts,jsx,tsx}',
    // The preset lists token names (e.g. `outline`) that are not class usages.
    '!../../packages/shared-ui/tailwind-preset.ts',
  ],
  plugins: [forms, containerQueries],
};

export default config;
