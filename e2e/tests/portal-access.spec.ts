import { apiAs } from '../support/api.ts';
import { URLS, type Role } from '../support/env.ts';
import { expect, test } from '../support/fixtures.ts';

const CASES: { name: string; role: Role; portal: string; portalPath: string; message: RegExp; apiPath: string }[] = [
  {
    name: 'courierCannotOpenTheLogisticsPortal',
    role: 'courier',
    portal: URLS.logistics,
    portalPath: '/#/portal/depos',
    message: /A belépéshez admin vagy logistics-user szerepkör szükséges/,
    apiPath: '/api/logistics/depos',
  },
  {
    name: 'senderCannotOpenTheCourierPortal',
    role: 'sender',
    portal: URLS.courier,
    portalPath: '/#/portal/allocated-packages',
    message: /A belépéshez admin vagy courier-user szerepkör szükséges/,
    apiPath: '/api/courier/me',
  },
];

for (const { name, role, portal, portalPath, message, apiPath } of CASES) {
  test(name, async ({ pageAs }) => {
    const page = await pageAs(role);

    await page.goto(`${portal}${portalPath}`);

    // Sent back to the landing page, which says why.
    await expect(page).toHaveURL(`${portal}/#/`);
    await expect(page.getByText(message)).toBeVisible();
    await expect(page.getByRole('link', { name: 'Portál megnyitása' })).toBeHidden();

    const api = await apiAs(role);
    expect((await api.get(apiPath)).status(), 'the backend refuses too').toBe(403);
    await api.dispose();
  });
}
