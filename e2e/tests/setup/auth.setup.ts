import { expect, test as setup } from '@playwright/test';
import { URLS, USERS, storageStatePath, type Role } from '../../support/env.ts';
import { mockExternalServices } from '../../support/external-mocks.ts';

// Each role logs in once on the real Keycloak page; the tests reuse the saved session.
for (const role of Object.keys(USERS) as Role[]) {
  const { email, password, loginAt } = USERS[role];

  setup(`${role} logs in on the Keycloak page`, async ({ page, context }) => {
    await mockExternalServices(context);
    await page.goto(loginAt);
    await page.getByRole('button', { name: 'Bejelentkezés' }).first().click();
    // The logo of our own theme.
    await expect(page.getByRole('img', { name: 'micro-futár logó' }).first()).toBeVisible();

    await page.getByLabel(/e-?mail|felhasználónév/i).fill(email);
    await page.getByLabel(/^(jelszó|password)/i).fill(password);
    await page.getByRole('button', { name: /bejelentkez|belépés|sign in|log in/i }).click();

    await page.waitForURL((url) => url.origin === loginAt);
    // The three landing pages differ; the gateway session is what the tests need.
    const me = await context.request.get(`${URLS.gateway}/api/auth/me`);
    expect(await me.json()).toMatchObject({ email });
    await context.storageState({ path: storageStatePath(role) });
  });
}
