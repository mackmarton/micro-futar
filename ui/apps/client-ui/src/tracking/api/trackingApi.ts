import { Api, type TrackingDTO } from '@package/shared-core/api/TrackingApiClient';

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

const trackingApiClient = new Api({
  baseUrl: resolveApiBaseUrl(),
  baseApiParams: {
    credentials: 'include',
  },
});

export const fetchTrackingByParcelNumber = async (
  parcelNumber: string,
  signal?: AbortSignal,
): Promise<TrackingDTO | null> => {
  const normalizedParcelNumber = parcelNumber.trim();

  if (!normalizedParcelNumber) {
    return null;
  }

  const response = await trackingApiClient.api.trackPackage(normalizedParcelNumber, {
    signal,
    format: 'json',
  });

  return response.data ?? null;
};

