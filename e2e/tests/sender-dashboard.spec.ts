import { apiAs, eventually } from '../support/api.ts';
import { URLS, USERS } from '../support/env.ts';
import { GEOCODED_POINT } from '../support/external-mocks.ts';
import { expect, test } from '../support/fixtures.ts';
import { findDomesticReferences } from '../support/reference-data.ts';

test('senderSeesTheirOwnShipmentInTheList', async ({ pageAs }) => {
  const sender = await apiAs('sender');
  const { countryId, cityId, packageSizeId } = await eventually('reference data', () => findDomesticReferences(sender));
  const address = { latitude: GEOCODED_POINT.latitude, longitude: GEOCODED_POINT.longitude };
  const response = await sender.post('/api/orders/shipments/new', {
    data: {
      senderName: 'Szabó Dóra',
      senderEmail: USERS.sender.email,
      senderPhone: '+36701112233',
      senderLocationCountryId: countryId,
      senderLocationCityId: cityId,
      senderZip: '1052',
      senderAddress: 'Petőfi Sándor utca 9',
      senderLatitude: address.latitude,
      senderLongitude: address.longitude,
      recipientName: 'Varga Péter',
      recipientEmail: 'varga.peter@posta.hu',
      recipientPhone: '+36209998877',
      recipientLocationCountryId: countryId,
      recipientLocationCityId: cityId,
      recipientZip: '1136',
      recipientAddress: 'Pannónia utca 21',
      recipientLatitude: address.latitude,
      recipientLongitude: address.longitude,
      packageSizeId,
    },
  });
  expect(response.status()).toBe(201);
  const { shipment } = (await response.json()) as { shipment: { parcelNumber: string } };
  await sender.dispose();

  const page = await pageAs('sender');
  await page.goto(`${URLS.client}/portal/dashboard`);

  const row = page.getByRole('row').filter({ hasText: shipment.parcelNumber });
  await expect(row).toContainText('Pannónia utca 21');
  await expect(row).toContainText('Fizetésre vár');
});
