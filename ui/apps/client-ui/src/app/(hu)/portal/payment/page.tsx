import type { Metadata } from 'next';
import { PaymentPage } from '../../../../payment/PaymentPage.tsx';

export const metadata: Metadata = {
  title: 'Fizetés',
  robots: { index: false, follow: false },
};

export default function PaymentRoute() {
  return <PaymentPage locale="hu" />;
}
