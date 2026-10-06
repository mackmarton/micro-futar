import type { Metadata } from 'next';
import { TrackPackagePage } from '../../../tracking/TrackPackagePage.tsx';

export const metadata: Metadata = {
  title: 'Csomagkövetés',
  description:
    'Kövesse nyomon küldeménye útját valós időben a felvételtől a kézbesítésig – csak adja meg a követési számot.',
  // A `?trackingNumber=...` változatok mind erre az egy címre kanonizálódnak.
  alternates: { canonical: '/portal/tracking' },
};

export default function TrackingRoute() {
  return <TrackPackagePage />;
}
