import type { APIRequestContext } from '@playwright/test';
import { MASTER_DATA } from '../scripts/seed.mjs';
import { getJson } from './api.ts';

type Named = { id: number; name: string };

/** The ids of the seeded domestic route, as orders knows them; undefined while they have not all arrived. */
export async function findDomesticReferences(orders: APIRequestContext) {
  const [countries, cities, packageSizes] = await Promise.all([
    getJson<(Named & { isoCode?: string })[]>(orders, '/api/orders/countries'),
    getJson<(Named & { countryId: number; bboxMinLatitude?: number })[]>(orders, '/api/orders/cities'),
    getJson<Named[]>(orders, '/api/orders/package-sizes'),
  ]);
  const country = countries.find((candidate) => candidate.name === MASTER_DATA.countries[0].name);
  // The geo data matters: without it orders would not check the coordinates of a shipment.
  const city = cities.find((c) => c.countryId === country?.id && c.name === 'Budapest' && c.bboxMinLatitude != null);
  const packageSize = packageSizes.find((candidate) => candidate.name === MASTER_DATA.packageSize.name);
  if (!country || !city || !packageSize) return undefined;
  return { countryId: country.id, cityId: city.id, packageSizeId: packageSize.id };
}
