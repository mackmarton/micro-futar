import type { Metadata } from 'next';
import { CreateOrderPage } from '../../../create-order/CreateOrderPage.tsx';

export const metadata: Metadata = {
  title: 'Csomag feladása',
  robots: { index: false, follow: true },
};

export default function CreateOrderRoute() {
  return <CreateOrderPage />;
}
