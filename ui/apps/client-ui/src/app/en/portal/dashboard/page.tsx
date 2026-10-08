import type { Metadata } from 'next';
import { DashboardPage } from '../../../../my-shipments/DashboardPage.tsx';

export const metadata: Metadata = {
  title: 'My packages',
  robots: { index: false, follow: true },
};

export default function DashboardRoute() {
  return <DashboardPage locale="en" />;
}
