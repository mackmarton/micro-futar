import { gatewayApiConfig } from '@package/shared-core';
import { Api, type MyAssignmentDTO } from '@package/shared-core/api/CourierApiClient';

const courierApiClient = new Api(gatewayApiConfig());

/** The courier's own assignments of today, with the route and shipment details needed to carry them out. */
export const fetchMyAssignments = async (signal?: AbortSignal): Promise<MyAssignmentDTO[]> => {
  const response = await courierApiClient.api.findMyAssignments({
    signal,
    format: 'json',
  });
  return Array.isArray(response.data) ? response.data : [];
};
