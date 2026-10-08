import type { Metadata, Viewport } from 'next';
import { Inter, Manrope } from 'next/font/google';
import Script from 'next/script';
import type { ReactNode } from 'react';
import { Footer } from '@package/shared-ui/Footer';
import { Providers } from '../providers.tsx';
import { SITE_DESCRIPTION_EN, SITE_NAME, SITE_URL } from '../site.ts';
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
    default: `${SITE_NAME} – online parcel shipping and tracking`,
    template: `%s | ${SITE_NAME}`,
  },
  description: SITE_DESCRIPTION_EN,
  applicationName: SITE_NAME,
  icons: { icon: '/micro-futar-logo.svg' },
  openGraph: {
    type: 'website',
    locale: 'en_US',
    siteName: SITE_NAME,
    url: '/en',
    title: SITE_NAME,
    description: SITE_DESCRIPTION_EN,
    images: [{ url: '/storage.webp', alt: 'Modern logistics warehouse with automated shelving' }],
  },
  twitter: {
    card: 'summary_large_image',
    title: SITE_NAME,
    description: SITE_DESCRIPTION_EN,
    images: ['/storage.webp'],
  },
};

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
};

/**
 * Saját gyökér-layout a magyar (hu) group mellett ("multiple root layouts" App Router
 * mintázat) — route group helyett külön `<html lang>`-ra van szükség, ezért ez nem a
 * `(hu)/layout.tsx` alá, hanem vele egy szinten, önálló `<html>`-lel él. Az URL-prefix (`/en`)
 * ebből a mappanévből jön, a magyar route-ok pedig a `(hu)` csoport miatt prefix nélkül maradnak.
 */
export default function EnglishRootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en" className={`${manrope.variable} ${inter.variable}`}>
      <head>
        {/* eslint-disable-next-line @next/next/no-page-custom-font */}
        <link
          rel="stylesheet"
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap"
        />
      </head>
      <body>
        <Script src="/app-env.js" strategy="beforeInteractive" />
        <Providers locale="en">
          {children}
          <Footer />
        </Providers>
      </body>
    </html>
  );
}
