import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: [
        'micro-futar-logo.svg',
        'icons/pwa-192x192.png',
        'icons/pwa-512x512.png',
        'icons/pwa-192x192-maskable.png',
        'icons/pwa-512x512-maskable.png',
        'icons/apple-touch-icon.png',
      ],
      manifest: {
        name: 'micro-futar Courier',
        short_name: 'micro-futar',
        description: 'micro-futar courier portal',
        theme_color: '#0c9488',
        background_color: '#f8fafc',
        display: 'standalone',
        start_url: '/#/',
        scope: '/',
        icons: [
          {
            src: 'icons/pwa-192x192.png',
            sizes: '192x192',
            type: 'image/png',
          },
          {
            src: 'icons/pwa-512x512.png',
            sizes: '512x512',
            type: 'image/png',
          },
          {
            src: 'icons/pwa-192x192-maskable.png',
            sizes: '192x192',
            type: 'image/png',
            purpose: 'maskable',
          },
          {
            src: 'icons/pwa-512x512-maskable.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'maskable',
          },
        ],
      },
    }),
  ],
})
