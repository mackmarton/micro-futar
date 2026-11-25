import { Api, type ShipmentDTO } from '@package/shared-core/api/OrdersApiClient';
import { gatewayApiConfig } from '@package/shared-core';

const ordersApiClient = new Api(gatewayApiConfig());

export const fetchShipmentsForUser = async (signal?: AbortSignal): Promise<ShipmentDTO[]> => {
  const response = await ordersApiClient.api.getShipmentsForUser({
    signal,
    format: 'json',
  });

  return Array.isArray(response.data) ? response.data : [];
};

