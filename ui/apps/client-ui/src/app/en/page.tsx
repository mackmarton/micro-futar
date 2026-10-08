import { LandingPage } from '../../landing/LandingPage.tsx';
import { SITE_DESCRIPTION_EN, SITE_NAME, SITE_URL } from '../site.ts';

const organizationJsonLd = {
  '@context': 'https://schema.org',
  '@type': 'Organization',
  name: SITE_NAME,
  url: `${SITE_URL}/en`,
  logo: `${SITE_URL}/micro-futar-logo.svg`,
  description: SITE_DESCRIPTION_EN,
};

export const metadata = {
  alternates: { canonical: '/en', languages: { hu: '/', en: '/en' } },
};

export default function HomePage() {
  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(organizationJsonLd) }}
      />
      <LandingPage locale="en" />
    </>
  );
}
