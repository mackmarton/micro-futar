import { resolveApiBaseUrl } from '@package/shared-core';
import { Api, type MyAssignmentDTO } from '@package/shared-core/api/CourierApiClient';
import { fetchMyAssignments } from '../../myAssignmentsApi.ts';

export type AssignmentType = 'Pickup' | 'Delivery';

export type CourierAllocation = {
  assignmentId: number | null;
  shipmentRouteId: number | null;
  parcelNumber: string;
  contactName: string;
  contactPhone: string;
  routeAddress: string;
  assignmentType: AssignmentType;
  pickedUpForDelivery: boolean;
  failed: boolean;
  latitude: number;
  longitude: number;
};

const courierApiClient = new Api({
  baseUrl: resolveApiBaseUrl(),
  baseApiParams: {
    credentials: 'include',
  },
});

export const pickUpParcel = async (assignmentId: number): Promise<void> => {
  await courierApiClient.api.pickUpParcel(assignmentId);
};

export const fulfillShipmentRouteAssignment = async (assignmentId: number): Promise<void> => {
  await courierApiClient.api.fulfillShipmentRouteAssignment(assignmentId);
};

export const failShipmentRouteAssignment = async (assignmentId: number): Promise<void> => {
  await courierApiClient.api.failShipmentRouteAssignment(assignmentId);
};

const isFiniteCoordinate = (value: number | null | undefined): value is number => {
  return typeof value === 'number' && Number.isFinite(value);
};

// Depók közötti szakasz, illetve cím vagy koordináta nélküli tétel nem jelenik meg a listában és a térképen.
const toAllocation = (assignment: MyAssignmentDTO): CourierAllocation | null => {
  const { assignmentType, address, latitude, longitude } = assignment;

  if (!assignmentType || !address || !isFiniteCoordinate(latitude) || !isFiniteCoordinate(longitude)) {
    return null;
  }

  return {
    assignmentId: assignment.assignmentId ?? null,
    shipmentRouteId: assignment.shipmentRouteId ?? null,
    parcelNumber: assignment.parcelNumber?.trim() || '-',
    contactName: assignment.contactName?.trim() || '-',
    contactPhone: assignment.contactPhone?.trim() || '-',
    routeAddress: address,
    assignmentType: assignmentType === 'PICKUP' ? 'Pickup' : 'Delivery',
    pickedUpForDelivery: Boolean(assignment.pickedUpForDelivery),
    failed: Boolean(assignment.failed),
    latitude,
    longitude,
  };
};

export const fetchCourierAllocationsForCurrentDay = async (signal?: AbortSignal): Promise<CourierAllocation[]> => {
  const assignments = await fetchMyAssignments(signal);

  return assignments
    .map(toAllocation)
    .filter((allocation): allocation is CourierAllocation => allocation !== null);
};
