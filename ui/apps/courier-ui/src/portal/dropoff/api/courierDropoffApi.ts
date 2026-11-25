import { gatewayApiConfig } from '@package/shared-core';
import {
  Api,
  type ShipmentRouteCourierDTO,
} from '@package/shared-core/api/CourierApiClient';
import { fetchMyAssignments } from '../../myAssignmentsApi.ts';

const courierApiClient = new Api(gatewayApiConfig());

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

  const myAssignments = await fetchMyAssignments(signal);
  const fulfilledIds = new Set(
    myAssignments.filter((details) => details.fulfillmentTime).map((details) => details.assignmentId),
  );

  return countCandidates.filter((assignment) => !fulfilledIds.has(assignment.id)).length;
};

export const fulfillAllPickupsForCurrentDay = async (): Promise<void> => {
  await courierApiClient.api.fulfillAllPickupsForCurrentDay();
};
