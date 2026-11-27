import type { APIRequestContext, Page } from '@playwright/test';
import { MASTER_DATA } from '../scripts/seed.mjs';
import { apiAs, eventually, getJson } from '../support/api.ts';
import { URLS } from '../support/env.ts';
import { GEOCODED_POINT } from '../support/external-mocks.ts';
import { expect, test } from '../support/fixtures.ts';
import { sendPaymentAuthorized } from '../support/stripe-webhook.ts';

type Party = {
  card: string;
  location: 'sender-location' | 'recipient-location';
  name: string;
  phone: string;
  email: string;
  zip: string;
  street: string;
};

type Route = { id: number; shipmentId: number; routePartNumber: number; fulfillmentTime?: string };
type Assignment = { assignmentId: number; assignmentType?: string; parcelNumber?: string; latitude?: number };

const SENDER: Party = {
  card: 'Feladó adatai',
  location: 'sender-location',
  name: 'Nagy Eszter',
  phone: '+36201234567',
  email: 'nagy.eszter@posta.hu',
  zip: '1062',
  street: 'Andrássy út 60',
};

const RECIPIENT: Party = {
  card: 'Címzett adatai',
  location: 'recipient-location',
  name: 'Tóth Gábor',
  phone: '+36307654321',
  email: 'toth.gabor@posta.hu',
  zip: '1114',
  street: 'Bartók Béla út 52',
};

async function fillAddress(page: Page, party: Party): Promise<void> {
  const card = page.locator('section').filter({ has: page.getByRole('heading', { name: party.card }) });
  await card.getByLabel(/^Név/).fill(party.name);
  await card.getByLabel(/^Telefonszám/).fill(party.phone);
  await card.getByLabel(/^Email cím/).fill(party.email);
  await card.getByRole('combobox', { name: /^Ország/ }).click();
  await card.getByRole('option', { name: MASTER_DATA.countries[0].name }).click();
  await card.getByRole('combobox', { name: /^Város/ }).click();
  await card.getByRole('option', { name: 'Budapest' }).click();
  await card.getByLabel(/^Irányítószám/).fill(party.zip);

  // The map follows the typed address after a pause. Confirming has to wait for that, or the late answer would
  // move the pin back and drop the confirmation.
  const geocoded = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return url.hostname === 'nominatim.openstreetmap.org' && url.searchParams.get('q')!.startsWith(party.street);
  });
  await card.getByLabel(/^Cím/).fill(party.street);
  await geocoded;

  const location = page.getByTestId(party.location);
  const markedPoint = location.getByText(/^Jelölt pont:/);
  const geocodedPoint = `${GEOCODED_POINT.latitude.toFixed(6)}, ${GEOCODED_POINT.longitude.toFixed(6)}`;
  await expect(markedPoint).toContainText(geocodedPoint);

  // Move the pin a few streets away by clicking the map (Leaflet renders no roles to locate it by).
  await location.locator('.leaflet-container').click({ position: { x: 200, y: 120 } });
  await expect(markedPoint).not.toContainText(geocodedPoint);
  await location.getByRole('button', { name: 'Pin megerősítése' }).click();
  await expect(location.getByText(/koordinátái még nincsenek megerősítve/)).toBeHidden();
}

/** The dispatcher starts the assignment of the depot's open legs to couriers. */
async function planDepot(page: Page, depotId: number): Promise<void> {
  await page.goto(`${URLS.logistics}/#/portal/depos/${depotId}`);
  // A fresh page, so the success message below belongs to this run.
  await page.reload();
  await page.getByRole('button', { name: 'Szállítmány tervezés indítása' }).click();
  await expect(page.getByText('A szállítmány tervezés sikeresen lefutott.')).toBeVisible();
}

const findRoutes = async (dispatcher: APIRequestContext, shipmentId: number) =>
  (await getJson<Route[]>(dispatcher, '/api/logistics/shipment-routes'))
    .filter((route) => route.shipmentId === shipmentId)
    .sort((left, right) => left.routePartNumber - right.routePartNumber);

/** Waits until courier has the assignment together with its route and shipment. */
const waitForAssignment = (courier: APIRequestContext, parcelNumber: string, assignmentType: 'PICKUP' | 'DELIVERY') =>
  eventually(`${assignmentType} assignment of ${parcelNumber}`, async () =>
    (await getJson<Assignment[]>(courier, '/api/courier/me/assignments')).find(
      (assignment) =>
        assignment.parcelNumber === parcelNumber &&
        assignment.assignmentType === assignmentType &&
        assignment.latitude != null,
    ),
  );

/** From the courier's list to the details of one assignment, then takes over the parcel. */
async function openAndPickUp(page: Page, parcelNumber: string, type: 'Felvétel' | 'Kiszállítás'): Promise<void> {
  await page.goto(`${URLS.courier}/#/portal/allocated-packages`);
  await page.reload();
  await page.getByRole('row').filter({ hasText: parcelNumber }).filter({ hasText: type }).getByRole('link', { name: 'Megnyitás' }).click();
  await expect(page.getByRole('heading', { level: 1, name: 'Kiosztott csomag részletei' })).toBeVisible();
  // The map next to it repeats the number inside other text.
  await expect(page.getByRole('paragraph').filter({ hasText: new RegExp(`^${parcelNumber}$`) })).toBeVisible();
  await page.getByRole('button', { name: 'Csomag felvétele' }).click();
  await expect(page.getByText('Felvéve: Igen')).toBeVisible();
}

test('guestShipmentIsPaidAssignedAndDeliveredToTheRecipient', async ({ pageAs }) => {
  test.setTimeout(180_000);
  const guestApi = await apiAs('guest');
  const dispatcherApi = await apiAs('dispatcher');
  const courierApi = await apiAs('courier');
  const guest = await pageAs('guest');
  let shipment!: { id: number; parcelNumber: string; maxPrice: number };

  await test.step('a guest creates the shipment and starts the payment', async () => {
    // Stripe Checkout is not automated: the navigation to it ends on an empty page.
    await guest.route(`${URLS.stripeCheckout}**`, (route) =>
      route.fulfill({ contentType: 'text/html', body: '<!doctype html><title>Checkout</title>' }),
    );
    await guest.goto(`${URLS.client}/portal/create-order`);
    await fillAddress(guest, SENDER);
    await fillAddress(guest, RECIPIENT);
    await guest.getByRole('button', { name: /Maximum oldalhossz:\s*30 cm/ }).click();

    const created = guest
      .waitForResponse((response) => response.url().endsWith('/api/orders/shipments/new'))
      .then(async (response) => {
        expect(response.status()).toBe(201);
        return ((await response.json()) as { shipment: typeof shipment }).shipment;
      });
    await guest.getByRole('button', { name: 'Tovább a fizetéshez' }).click();
    shipment = await created;
    await guest.waitForURL(`${URLS.stripeCheckout}**`);
  });

  await test.step('Stripe reports the authorization and the guest returns to the success page', async () => {
    await sendPaymentAuthorized(guestApi, { shipmentId: shipment.id, amount: shipment.maxPrice });
    await guest.goto(`${URLS.client}/portal/payment?shipmentId=${shipment.id}&outcome=success`);
    // Logistics prices the shipment right away, so it may already be captured.
    await expect(guest.getByRole('heading', { name: /^(Sikeres fizetés|Kifizetve)$/ })).toBeVisible({ timeout: 30_000 });
  });

  const depots = await getJson<{ id: number; name: string }[]>(dispatcherApi, '/api/logistics/depos');
  const depot = depots.find((candidate) => candidate.name === MASTER_DATA.depots[0].name)!;
  const dispatcher = await pageAs('dispatcher');
  const courier = await pageAs('courier');

  await test.step('the dispatcher assigns the pickup leg', async () => {
    await eventually('both legs planned', async () => {
      const routes = await findRoutes(dispatcherApi, shipment.id);
      return routes.length === 2 ? routes : undefined;
    });
    await planDepot(dispatcher, depot.id);
  });

  await test.step('the courier collects the parcel and drops it at the depot', async () => {
    await waitForAssignment(courierApi, shipment.parcelNumber, 'PICKUP');
    await openAndPickUp(courier, shipment.parcelNumber, 'Felvétel');

    await courier.goto(`${URLS.courier}/#/portal/shipment-dropoff`);
    const droppedOff = courier.waitForResponse((response) => response.url().includes('/fulfill-all-pickups'));
    await courier.getByRole('button', { name: 'Összes leadása' }).click();
    expect((await droppedOff).status()).toBe(200);
    await expect(courier.getByRole('row').filter({ hasText: shipment.parcelNumber })).toContainText('Depóban leadva');
  });

  await test.step('the dispatcher assigns the delivery leg', async () => {
    await eventually('pickup leg fulfilled in logistics', async () =>
      (await findRoutes(dispatcherApi, shipment.id))[0].fulfillmentTime ? true : undefined,
    );
    await planDepot(dispatcher, depot.id);
  });

  await test.step('the courier delivers the parcel', async () => {
    const assignment = await waitForAssignment(courierApi, shipment.parcelNumber, 'DELIVERY');
    await openAndPickUp(courier, shipment.parcelNumber, 'Kiszállítás');

    const fulfilled = courier.waitForResponse((response) =>
      response.url().endsWith(`/shipment-route-couriers/${assignment.assignmentId}/fulfill`),
    );
    await courier.getByRole('button', { name: 'Szállítás nyugtázása' }).click();
    expect((await fulfilled).status()).toBe(200);
  });

  await test.step('the public tracking page shows the shipment as delivered', async () => {
    await eventually('delivery in tracking', async () => {
      const response = await guestApi.get(`/api/tracking/track/${shipment.parcelNumber}`);
      if (response.status() !== 200) return undefined;
      const tracking = (await response.json()) as { trackingParts: Record<string, { destination: boolean }> };
      return Object.values(tracking.trackingParts).some((part) => part.destination) ? true : undefined;
    });
    await guest.goto(`${URLS.client}/portal/tracking`);
    await guest.getByRole('textbox', { name: /követési számot/ }).fill(shipment.parcelNumber);
    await guest.getByRole('button', { name: 'Keresés' }).click();
    await expect(guest.getByRole('heading', { name: shipment.parcelNumber })).toBeVisible();
    await expect(guest.getByText('Kézbesítve', { exact: true })).toBeVisible();
  });

  await Promise.all([guestApi.dispose(), dispatcherApi.dispose(), courierApi.dispose()]);
});
