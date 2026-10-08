import type { Metadata, Viewport } from 'next';
import { Inter, Manrope } from 'next/font/google';
import Script from 'next/script';
import type { ReactNode } from 'react';
import { Footer } from '@package/shared-ui/Footer';
import { Providers } from '../providers.tsx';
import { SITE_DESCRIPTION, SITE_NAME, SITE_URL } from '../site.ts';
import '../../index.css';

const manrope = Manrope({
  subsets: ['latin', 'latin-ext'],
  weight: ['400', '600', '700', '800'],
  variable: '--font-manrope',
  display: 'swap',
});

const inter = Inter({
  subsets: ['latin', 'latin-ext'],
  weight: ['400', '500', '600'],
  variable: '--font-inter',
  display: 'swap',
});

export const metadata: Metadata = {
  metadataBase: new URL(SITE_URL),
  title: {
    default: `${SITE_NAME} – online csomagfeladás és csomagkövetés`,
    template: `%s | ${SITE_NAME}`,
  },
  description: SITE_DESCRIPTION,
  applicationName: SITE_NAME,
  icons: { icon: '/micro-futar-logo.svg' },
  openGraph: {
    type: 'website',
    locale: 'hu_HU',
    siteName: SITE_NAME,
    url: '/',
    title: SITE_NAME,
    description: SITE_DESCRIPTION,
    images: [{ url: '/storage.webp', alt: 'Modern logisztikai raktár automatizált polcrendszerrel' }],
  },
  twitter: {
    card: 'summary_large_image',
    title: SITE_NAME,
    description: SITE_DESCRIPTION,
    images: ['/storage.webp'],
  },
};

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="hu" className={`${manrope.variable} ${inter.variable}`}>
      <head>
        {/* Az ikonfont változtatható tengelyei miatt (FILL, wght) ez nem next/font-on keresztül töltődik. */}
        {/* eslint-disable-next-line @next/next/no-page-custom-font */}
        <link
          rel="stylesheet"
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap"
        />
      </head>
      <body>
        {/*
          Futásidejű konfiguráció (pl. API base URL): a konténer indulásakor a
          docker-entrypoint.d/20-write-app-env.sh írja felül. Minden app bundle előtt le kell
          futnia, mert a shared-core API kliensei modul-betöltéskor olvassák ki.
        */}
        <Script src="/app-env.js" strategy="beforeInteractive" />
        <Providers locale="hu">
          {children}
          <Footer />
        </Providers>
      </body>
    </html>
  );
}
