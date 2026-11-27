#!/usr/bin/env node
// Creates the data the e2e tests and a manual try-out need: currencies, region, countries, cities, depots, depot
// transits, base fees, package size, vehicle and the courier record of courier@e2e.test. Logs in through the gateway
// as the dispatcher, like the browser does. Safe to run repeatedly. No dependencies.
// `node e2e/scripts/seed.mjs --check` only logs in with all three users and calls the API as each of them.
// The Playwright data setup imports `seed` and `MASTER_DATA` from here.
import http from 'node:http';
import { pathToFileURL } from 'node:url';

const GATEWAY = 'http://localhost:8085';
const CSRF = { 'X-Requested-With': 'micro-futar' };
export const MASTER_DATA = {
  region: { name: 'Közép-Európa' },
  currencies: [
    { code: 'HUF', name: 'Magyar forint', symbol: 'Ft' },
    { code: 'EUR', name: 'Euró', symbol: '€' },
  ],
  countries: [
    { name: 'Magyarország', isoCode: 'HU', currencyCode: 'HUF' },
    { name: 'Ausztria', isoCode: 'AT', currencyCode: 'EUR' },
  ],
  // One depot per country, so a domestic shipment always has the same two legs: sender -> depot -> recipient.
  depots: [
    {
      isoCode: 'HU',
      city: 'Budapest',
      name: 'Budapest Angyalföld depó',
      zip: '1134',
      address: 'Váci út 45',
      latitude: 47.5256,
      longitude: 19.0626,
    },
    {
      isoCode: 'AT',
      city: 'Bécs',
      name: 'Wien Erdberg depó',
      zip: '1030',
      address: 'Erdberger Lände 28',
      latitude: 48.2012,
      longitude: 16.4122,
    },
  ],
  packageSize: { name: 'S', maxLength: 30 },
  baseFeeByCountry: { HU: 1990, AT: 4490 },
  depotTransitPrice: 3500,
  vehicle: { registrationNumber: 'AEF-421', maximumPackableVolume: 10_000_000 },
};

const USERS = {
  sender: ['sender@e2e.test', 'e2e-sender-pass'],
  dispatcher: ['dispatcher@e2e.test', 'e2e-dispatcher-pass'],
  courier: ['courier@e2e.test', 'e2e-courier-pass'],
};

// `keycloak` is the Docker host from here; resolving it ourselves works even without the /etc/hosts entry.
const lookup = (hostname, options, callback) => {
  if (options.all) callback(null, [{ address: '127.0.0.1', family: 4 }]);
  else callback(null, '127.0.0.1', 4);
};

function send(url, { method = 'GET', headers = {}, body } = {}) {
  return new Promise((resolve, reject) => {
    const request = http.request(url, { method, headers, lookup }, (response) => {
      let text = '';
      response.setEncoding('utf8');
      response.on('data', (chunk) => (text += chunk));
      response.on('end', () => resolve({ status: response.statusCode, headers: response.headers, text }));
    });
    request.on('error', reject);
    request.end(body);
  });
}

/** A minimal browser: cookies per host, redirects followed. */
function session() {
  const jars = new Map();
  return async function call(url, options = {}) {
    let target = new URL(url);
    let next = options;
    for (let hop = 0; hop < 10; hop++) {
      const jar = jars.get(target.host) ?? new Map();
      jars.set(target.host, jar);
      const cookie = [...jar].map(([name, value]) => `${name}=${value}`).join('; ');
      const response = await send(target, { ...next, headers: { ...next.headers, ...(cookie && { cookie }) } });
      for (const header of response.headers['set-cookie'] ?? []) {
        const [pair] = header.split(';');
        const at = pair.indexOf('=');
        jar.set(pair.slice(0, at), pair.slice(at + 1));
      }
      const location = response.headers.location;
      // stop before the UI: the login is complete once the gateway sends us there
      if (response.status < 300 || response.status >= 400 || !location) return { ...response, url: target };
      target = new URL(location, target);
      if (target.port.startsWith('517')) return { ...response, url: target };
      next = { headers: {} };
    }
    throw new Error(`Too many redirects from ${url}`);
  };
}

async function login([username, password]) {
  const call = session();
  const page = await call(`${GATEWAY}/oauth2/authorization/keycloak`);
  const action = page.text.match(/"?loginAction"?\s*:\s*"([^"]+)"/)?.[1];
  if (!action) throw new Error(`No login form for ${username} (HTTP ${page.status} at ${page.url})`);
  const form = new URLSearchParams({ username, password }).toString();
  const result = await call(action.replaceAll('\\/', '/').replaceAll('&amp;', '&'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Content-Length': Buffer.byteLength(form) },
    body: form,
  });
  if (!result.url.port.startsWith('517')) throw new Error(`Login failed for ${username} (HTTP ${result.status})`);
  return call;
}

async function getJson(call, path) {
  const response = await call(`${GATEWAY}${path}`);
  if (response.status !== 200) throw new Error(`GET ${path}: HTTP ${response.status} ${response.text}`);
  return JSON.parse(response.text);
}

async function sendJson(call, method, path, body) {
  const text = JSON.stringify(body);
  const response = await call(`${GATEWAY}${path}`, {
    method,
    headers: { ...CSRF, 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(text) },
    body: text,
  });
  if (response.status !== 200 && response.status !== 201) {
    throw new Error(`${method} ${path}: HTTP ${response.status} ${response.text}`);
  }
  return JSON.parse(response.text);
}

/** The existing entity that matches, or a newly created one. */
async function ensure(call, path, matches, body) {
  const existing = (await getJson(call, path)).find(matches);
  if (existing) return existing;
  const created = await sendJson(call, 'POST', path, body);
  console.log(`Created ${path} ${body.name ?? body.code ?? body.registrationNumber ?? created.id}`);
  return created;
}

export async function seed() {
  const dispatcher = await login(USERS.dispatcher);
  const api = '/api/logistics';

  for (const currency of MASTER_DATA.currencies) {
    await ensure(dispatcher, `${api}/currencies`, (c) => c.code === currency.code, currency);
  }
  const region = await ensure(dispatcher, `${api}/regions`, (r) => r.name === MASTER_DATA.region.name, MASTER_DATA.region);

  const countries = {};
  for (const country of MASTER_DATA.countries) {
    countries[country.isoCode] = await ensure(dispatcher, `${api}/countries`, (c) => c.isoCode === country.isoCode, {
      ...country,
      regionId: region.id,
    });
  }

  const packageSize = await ensure(
    dispatcher,
    `${api}/package-sizes`,
    (p) => p.name === MASTER_DATA.packageSize.name,
    MASTER_DATA.packageSize,
  );

  // Base fees come before the depots and transits: every one of these writes recalculates the country prices.
  for (const [isoCode, price] of Object.entries(MASTER_DATA.baseFeeByCountry)) {
    const countryId = countries[isoCode].id;
    await ensure(
      dispatcher,
      `${api}/country-base-fees`,
      (fee) => fee.countryId === countryId && fee.packageSizeId === packageSize.id,
      { countryId, packageSizeId: packageSize.id, price },
    );
  }

  const depots = {};
  for (const { isoCode, city, ...depot } of MASTER_DATA.depots) {
    const countryId = countries[isoCode].id;
    // The city is geocoded by logistics on creation (ORS mock); orders validates shipment coordinates against it.
    const { id: locationCityId } = await ensure(
      dispatcher,
      `${api}/cities`,
      (c) => c.countryId === countryId && c.name === city,
      { countryId, name: city },
    );
    depots[isoCode] = await ensure(dispatcher, `${api}/depos`, (d) => d.name === depot.name, {
      ...depot,
      locationCountryId: countryId,
      locationCityId,
    });
  }

  for (const [from, to] of [
    ['HU', 'AT'],
    ['AT', 'HU'],
  ]) {
    const transit = {
      originDepoId: depots[from].id,
      destinationDepoId: depots[to].id,
      packageSizeId: packageSize.id,
    };
    await ensure(
      dispatcher,
      `${api}/depo-transits`,
      (t) => Object.entries(transit).every(([key, value]) => t[key] === value),
      { ...transit, transportType: 'ROAD', price: MASTER_DATA.depotTransitPrice },
    );
  }

  const vehicle = await ensure(
    dispatcher,
    `${api}/vehicles`,
    (v) => v.registrationNumber === MASTER_DATA.vehicle.registrationNumber,
    MASTER_DATA.vehicle,
  );

  const courier = {
    name: 'Kovács Csaba',
    email: USERS.courier[0],
    telephone: '+36301234567',
    qualifiedFor: 'ROAD',
    courierType: 'DELIVERY',
    depoId: depots.HU.id,
    vehicleId: vehicle.id,
  };
  const existing = await ensure(dispatcher, `${api}/couriers`, (c) => c.email === courier.email, courier);
  if (existing.depoId !== courier.depoId || existing.vehicleId !== courier.vehicleId) {
    await sendJson(dispatcher, 'PUT', `${api}/couriers/${existing.id}`, { ...courier, id: existing.id });
    console.log('Courier record updated with depot and vehicle');
  }
  console.log('Seed complete');
}

async function check() {
  const checks = {
    sender: ['/api/auth/me', '/api/orders/shipments'],
    dispatcher: ['/api/auth/me', '/api/logistics/couriers', '/api/logistics/depos'],
    courier: ['/api/auth/me', '/api/courier/me'],
  };
  for (const [user, paths] of Object.entries(checks)) {
    const call = await login(USERS[user]);
    for (const path of paths) {
      console.log(`${user.padEnd(10)} GET ${path} -> 200 ${JSON.stringify(await getJson(call, path))}`);
    }
  }
}

if (import.meta.url === pathToFileURL(process.argv[1] ?? '').href) {
  await (process.argv.includes('--check') ? check() : seed()).catch((error) => {
    console.error(error.message);
    process.exit(1);
  });
}
