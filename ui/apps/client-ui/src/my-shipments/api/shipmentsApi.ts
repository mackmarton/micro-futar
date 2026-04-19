import { Api, type ShipmentDTO } from '@package/shared-core/api/OrdersApiClient';

type RuntimeEnv = {
  VITE_API_BASE_URL?: string;
  API_BASE_URL?: string;
};

const resolveApiBaseUrl = () => {
  const globalEnv = ((globalThis as { __APP_ENV__?: RuntimeEnv }).__APP_ENV__) ?? {};
  const processEnv = ((globalThis as { process?: { env?: RuntimeEnv } }).process?.env ?? {});

  return (
    globalEnv.VITE_API_BASE_URL ??
    globalEnv.API_BASE_URL ??
    processEnv.VITE_API_BASE_URL ??
    processEnv.API_BASE_URL ??
    'http://localhost:8085'
  );
};

const ordersApiClient = new Api({
  baseUrl: resolveApiBaseUrl(),
  baseApiParams: {
    credentials: 'include',
  },
});

export const fetchShipmentsForUser = async (signal?: AbortSignal): Promise<ShipmentDTO[]> => {
  const response = await ordersApiClient.api.getShipmentsForUser({
    signal,
    format: 'json',
  });

  return Array.isArray(response.data) ? response.data : [];
};

