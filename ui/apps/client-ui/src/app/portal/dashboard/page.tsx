import type { Metadata } from 'next';
import { DashboardPage } from '../../../my-shipments/DashboardPage.tsx';

export const metadata: Metadata = {
  title: 'Saját csomagjaim',
  robots: { index: false, follow: true },
};

export default function DashboardRoute() {
  return <DashboardPage />;
}
