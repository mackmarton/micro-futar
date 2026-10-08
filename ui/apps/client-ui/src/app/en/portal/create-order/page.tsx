import type { Metadata } from 'next';
import { CreateOrderPage } from '../../../../create-order/CreateOrderPage.tsx';

export const metadata: Metadata = {
  title: 'Send a package',
  robots: { index: false, follow: true },
};

export default function CreateOrderRoute() {
  return <CreateOrderPage locale="en" />;
}
