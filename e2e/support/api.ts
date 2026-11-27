import { expect, request, type APIRequestContext } from '@playwright/test';
import { URLS, storageStatePath, type Role } from './env.ts';

/** The gateway API with the session of a role (or as a guest), as the UIs call it. */
export function apiAs(role: Role | 'guest'): Promise<APIRequestContext> {
  return request.newContext({
    baseURL: URLS.gateway,
    extraHTTPHeaders: { 'X-Requested-With': 'micro-futar' },
    storageState: role === 'guest' ? undefined : storageStatePath(role),
  });
}

export async function getJson<T>(api: APIRequestContext, path: string): Promise<T> {
  const response = await api.get(path);
  expect(response.status(), `GET ${path}`).toBe(200);
  return (await response.json()) as T;
}

/** Polls until the data has arrived over Kafka; `find` returns undefined while it has not. */
export async function eventually<T>(description: string, find: () => Promise<T | undefined>): Promise<T> {
  let found: T | undefined;
  await expect
    .poll(async () => (found = await find()) !== undefined, {
      message: description,
      timeout: 60_000,
      intervals: [250, 500, 1_000, 2_000],
    })
    .toBe(true);
  return found as T;
}
