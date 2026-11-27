#!/usr/bin/env node
// Creates the data the test users need beyond their Keycloak accounts: a courier record for courier@e2e.test.
// Logs in through the gateway as the dispatcher, like the browser does. Safe to run repeatedly. No dependencies.
// `node e2e/scripts/seed.mjs --check` only logs in with all three users and calls the API as each of them.
import http from 'node:http';

const GATEWAY = 'http://localhost:8085';
const CSRF = { 'X-Requested-With': 'micro-futar' };
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

async function seed() {
  const dispatcher = await login(USERS.dispatcher);
  const couriers = await getJson(dispatcher, '/api/logistics/couriers');
  if (couriers.some((courier) => courier.email === USERS.courier[0])) {
    console.log('Courier record already exists');
    return;
  }
  const body = JSON.stringify({
    name: 'Csaba Courier',
    email: USERS.courier[0],
    telephone: '+36301234567',
    qualifiedFor: 'ROAD',
    courierType: 'DELIVERY',
  });
  const response = await dispatcher(`${GATEWAY}/api/logistics/couriers`, {
    method: 'POST',
    headers: { ...CSRF, 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) },
    body,
  });
  if (response.status !== 201) throw new Error(`Creating the courier: HTTP ${response.status} ${response.text}`);
  console.log('Courier record created');
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

await (process.argv.includes('--check') ? check() : seed()).catch((error) => {
  console.error(error.message);
  process.exit(1);
});
