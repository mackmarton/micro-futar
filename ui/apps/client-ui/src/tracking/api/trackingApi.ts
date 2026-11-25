import { Api, type TrackingDTO } from '@package/shared-core/api/TrackingApiClient';
import { gatewayApiConfig } from '@package/shared-core';

const trackingApiClient = new Api(gatewayApiConfig());

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

