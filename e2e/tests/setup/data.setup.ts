import { test as setup } from '@playwright/test';
import { MASTER_DATA, seed } from '../../scripts/seed.mjs';
import { apiAs, eventually, getJson } from '../../support/api.ts';
import { findDomesticReferences } from '../../support/reference-data.ts';

setup('master data is created by the dispatcher and reaches orders and courier', async () => {
  setup.setTimeout(180_000);
  await seed();

  const guest = await apiAs('guest');
  const references = await eventually('countries, cities and package sizes in orders', () =>
    findDomesticReferences(guest),
  );
  await eventually('domestic price in orders', async () => {
    const prices = await getJson<{ packageSizeId: number; maxPrice: number }[]>(
      guest,
      `/api/orders/country-prices?originCountryId=${references.countryId}&destinationCountryId=${references.countryId}`,
    );
    return prices.find(
      (price) => price.packageSizeId === references.packageSizeId && price.maxPrice === MASTER_DATA.baseFeeByCountry.HU,
    );
  });

  // courier has no public endpoint, so it is asked as the courier.
  const courier = await apiAs('courier');
  await eventually('courier record, depot and vehicle in courier', async () => {
    const response = await courier.get('/api/courier/me');
    if (response.status() !== 200) return undefined;
    const me = (await response.json()) as { depoId?: number; vehicle?: { registrationNumber?: string } };
    return me.depoId && me.vehicle?.registrationNumber === MASTER_DATA.vehicle.registrationNumber ? me : undefined;
  });
  await eventually('package size in courier', async () => {
    const sizes = await getJson<{ name: string }[]>(courier, '/api/courier/package-sizes');
    return sizes.find((size) => size.name === MASTER_DATA.packageSize.name);
  });

  await Promise.all([guest.dispose(), courier.dispose()]);
});
