import { Api, type TrackingDTO } from '@package/shared-core/api/TrackingApiClient';
import { resolveApiBaseUrl } from '@package/shared-core';

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

