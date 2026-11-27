import { createHmac, randomUUID } from 'node:crypto';
import { expect, type APIRequestContext } from '@playwright/test';
import { STRIPE_WEBHOOK_SECRET } from './env.ts';

/**
 * What Stripe sends once the sender has completed Checkout with manual capture: the card is authorized. This is the
 * event payments acts on (it does not handle checkout.session.completed). Signed like Stripe signs it.
 */
export async function sendPaymentAuthorized(
  api: APIRequestContext,
  { shipmentId, amount }: { shipmentId: number; amount: number },
): Promise<void> {
  const payload = JSON.stringify({
    id: `evt_${randomUUID()}`,
    object: 'event',
    // payments answers 500 to an event without api_version.
    api_version: '2025-01-27.acacia',
    type: 'payment_intent.amount_capturable_updated',
    data: {
      object: {
        id: `pi_${randomUUID().replaceAll('-', '')}`,
        object: 'payment_intent',
        // HUF goes to Stripe in hundredths.
        amount_capturable: Math.round(amount * 100),
        metadata: { shipmentId: String(shipmentId) },
      },
    },
  });
  const timestamp = Math.floor(Date.now() / 1000);
  const signature = createHmac('sha256', STRIPE_WEBHOOK_SECRET).update(`${timestamp}.${payload}`).digest('hex');

  const response = await api.post('/api/payments/webhooks/stripe', {
    headers: { 'Content-Type': 'application/json', 'Stripe-Signature': `t=${timestamp},v1=${signature}` },
    data: payload,
  });

  expect(response.status(), 'Stripe webhook').toBe(200);
}
