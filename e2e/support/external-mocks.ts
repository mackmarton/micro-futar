import type { BrowserContext } from '@playwright/test';

// Services the UIs call directly from the browser. The docker compose mocks only cover the backend's calls.
export const GEOCODED_POINT = { latitude: 47.4979, longitude: 19.0402 };

const BLANK_TILE = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=',
  'base64',
);

export async function mockExternalServices(context: BrowserContext): Promise<void> {
  await context.route('https://nominatim.openstreetmap.org/**', (route) =>
    route.fulfill({
      headers: { 'Access-Control-Allow-Origin': '*' },
      json: [{ lat: String(GEOCODED_POINT.latitude), lon: String(GEOCODED_POINT.longitude) }],
    }),
  );
  await context.route(/^https:\/\/([a-z]\.)?tile\.openstreetmap\.org\//, (route) =>
    route.fulfill({ contentType: 'image/png', body: BLANK_TILE }),
  );
  // The map embedded in the depot details of the logistics UI.
  await context.route('https://www.openstreetmap.org/export/**', (route) =>
    route.fulfill({ contentType: 'text/html', body: '<!doctype html><title>map</title>' }),
  );
}
