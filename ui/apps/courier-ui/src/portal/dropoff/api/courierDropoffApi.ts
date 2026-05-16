import { resolveApiBaseUrl } from '@package/shared-core';
import {
  Api,
  type ShipmentRouteCourierDTO,
} from '@package/shared-core/api/CourierApiClient';

const courierApiClient = new Api({
  baseUrl: resolveApiBaseUrl(),
  baseApiParams: {
    credentials: 'include',
  },
});

export const fetchCourierPickedUpAssignmentsForToday = async (signal?: AbortSignal): Promise<ShipmentRouteCourierDTO[]> => {
  const response = await courierApiClient.api.findAllPickedUpAssignmentsForCourierForCurrentDay({
    signal,
    format: 'json',
  });
  return Array.isArray(response.data) ? response.data : [];
};

export const countPendingDropoffsForAssignments = async (
  assignments: ShipmentRouteCourierDTO[],
  signal?: AbortSignal,
): Promise<number> => {
  const countCandidates = assignments.filter(
    (assignment) => assignment.pickedUpForDelivery === true && assignment.failed !== true,
  );

  if (countCandidates.length === 0) {
    return 0;
  }

  const routes = await Promise.all(
    countCandidates.map(async (assignment) => {
      if (typeof assignment.shipmentRouteId !== 'number') {
        return null;
      }

      try {
        const response = await courierApiClient.api.findById2(assignment.shipmentRouteId, {
          signal,
          format: 'json',
        });
        return response.data ?? null;
      } catch {
        return null;
      }
    }),
  );

  return routes.reduce((count, route) => {
    if (route?.fulfillmentTime) {
      return count;
    }

    return count + 1;
  }, 0);
};

export const fulfillAllPickupsForCurrentDay = async (): Promise<void> => {
  await courierApiClient.api.fulfillAllPickupsForCurrentDay();
};
