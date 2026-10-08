import type { MetadataRoute } from 'next';
import { SITE_URL } from './site.ts';

export const dynamic = 'force-static';

export default function sitemap(): MetadataRoute.Sitemap {
  return [
    { url: `${SITE_URL}/`, changeFrequency: 'monthly', priority: 1 },
    { url: `${SITE_URL}/en`, changeFrequency: 'monthly', priority: 1 },
    { url: `${SITE_URL}/portal/tracking`, changeFrequency: 'yearly', priority: 0.6 },
    { url: `${SITE_URL}/en/portal/tracking`, changeFrequency: 'yearly', priority: 0.6 },
  ];
}
