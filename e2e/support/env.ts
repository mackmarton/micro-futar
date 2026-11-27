// Addresses and test users of the docker compose environment; the users come from the test realm.
export const URLS = {
  client: 'http://localhost:5173',
  logistics: 'http://localhost:5174',
  courier: 'http://localhost:5175',
  gateway: 'http://localhost:8085',
  stripeCheckout: 'http://localhost:12111/checkout/',
} as const;

export const STRIPE_WEBHOOK_SECRET = process.env.STRIPE_WEBHOOK_SECRET ?? 'whsec_e2e_test_only';

export const USERS = {
  sender: { email: 'sender@e2e.test', password: 'e2e-sender-pass', loginAt: URLS.client },
  dispatcher: { email: 'dispatcher@e2e.test', password: 'e2e-dispatcher-pass', loginAt: URLS.logistics },
  courier: { email: 'courier@e2e.test', password: 'e2e-courier-pass', loginAt: URLS.courier },
} as const;

export type Role = keyof typeof USERS;

export const storageStatePath = (role: Role) => `.auth/${role}.json`;
