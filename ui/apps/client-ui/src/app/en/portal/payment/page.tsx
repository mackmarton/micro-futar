import type { Metadata } from 'next';
import { PaymentPage } from '../../../../payment/PaymentPage.tsx';

export const metadata: Metadata = {
  title: 'Payment',
  robots: { index: false, follow: false },
};

export default function PaymentRoute() {
  return <PaymentPage locale="en" />;
}
