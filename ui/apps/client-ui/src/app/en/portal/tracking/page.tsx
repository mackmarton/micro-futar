import type { Metadata } from 'next';
import { TrackPackagePage } from '../../../../tracking/TrackPackagePage.tsx';

export const metadata: Metadata = {
  title: 'Package tracking',
  description: 'Track your shipment in real time from pickup to delivery – just enter the tracking number.',
  alternates: { canonical: '/en/portal/tracking', languages: { hu: '/portal/tracking', en: '/en/portal/tracking' } },
};

export default function TrackingRoute() {
  return <TrackPackagePage locale="en" />;
}
