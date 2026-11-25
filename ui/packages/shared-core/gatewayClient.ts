import { CSRF_HEADER_NAME, CSRF_HEADER_VALUE, reportCsrfRejection } from './csrf';
import { resolveApiBaseUrl } from './runtime';

// `fetch` a gatewayhez. Más kiszolgálóhoz (pl. Nominatim) ne használd: a fejléc ott fölösleges preflightot okoz.
export const gatewayFetch: typeof fetch = async (input, init) => {
  const headers = new Headers(init?.headers ?? (input instanceof Request ? input.headers : undefined));
  headers.set(CSRF_HEADER_NAME, CSRF_HEADER_VALUE);

  const response = await fetch(input, { credentials: 'include', ...init, headers });

  if (response.status === 403) {
    // Másolatot olvasunk, hogy a hívó is feldolgozhassa a törzset.
    const body: unknown = await response.clone().json().catch(() => null);
    reportCsrfRejection(response.status, body, response.url);
  }

  return response;
};

// A generált kliensek közös beállítása: `new Api(gatewayApiConfig())`.
export const gatewayApiConfig = () => ({
  baseUrl: resolveApiBaseUrl(),
  baseApiParams: {
    credentials: 'include' as const,
  },
  customFetch: gatewayFetch,
});
