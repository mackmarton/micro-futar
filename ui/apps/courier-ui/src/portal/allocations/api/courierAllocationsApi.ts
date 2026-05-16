import { resolveApiBaseUrl } from '@package/shared-core';
import {
  Api,
  type ShipmentDTO,
  type ShipmentRouteCourierDTO,
  type ShipmentRouteDTO,
} from '@package/shared-core/api/CourierApiClient';

export type AssignmentType = 'Pickup' | 'Delivery';

export type CourierAllocation = {
  assignmentId: number | null;
  shipmentRouteId: number | null;
  shipmentId: number | null;
  parcelNumber: string;
  senderName: string;
  senderPhone: string;
  recipientName: string;
  recipientPhone: string;
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

const isNonEmpty = (value: string | null | undefined): value is string => {
  return typeof value === 'string' && value.trim().length > 0;
};

const isFiniteCoordinate = (value: number | null | undefined): value is number => {
  return typeof value === 'number' && Number.isFinite(value);
};

const toAssignmentTypeAndCoordinates = (
  route: ShipmentRouteDTO,
  shipment: ShipmentDTO,
): {
  assignmentType: AssignmentType;
  routeAddress: string;
  latitude: number;
  longitude: number;
} | null => {
  if (isNonEmpty(route.originAddress)) {
    if (!isFiniteCoordinate(shipment.senderLatitude) || !isFiniteCoordinate(shipment.senderLongitude)) {
      return null;
    }

    return {
      assignmentType: 'Pickup',
      routeAddress: route.originAddress,
      latitude: shipment.senderLatitude,
      longitude: shipment.senderLongitude,
    };
  }

  if (isNonEmpty(route.destinationAddress)) {
    if (!isFiniteCoordinate(shipment.recipientLatitude) || !isFiniteCoordinate(shipment.recipientLongitude)) {
      return null;
    }

    return {
      assignmentType: 'Delivery',
      routeAddress: route.destinationAddress,
      latitude: shipment.recipientLatitude,
      longitude: shipment.recipientLongitude,
    };
  }

  return null;
};

const fetchShipmentRouteById = async (shipmentRouteId: number, signal?: AbortSignal): Promise<ShipmentRouteDTO | null> => {
  const response = await courierApiClient.api.findById2(shipmentRouteId, {
    signal,
    format: 'json',
  });

  return response.data ?? null;
};

const fetchShipmentById = async (shipmentId: number, signal?: AbortSignal): Promise<ShipmentDTO | null> => {
  const response = await courierApiClient.api.findById1(shipmentId, {
    signal,
    format: 'json',
  });

  return response.data ?? null;
};

const hydrateAssignment = async (
  assignment: ShipmentRouteCourierDTO,
  signal?: AbortSignal,
): Promise<CourierAllocation | null> => {
  const shipmentRouteId = assignment.shipmentRouteId;

  if (typeof shipmentRouteId !== 'number') {
    return null;
  }

  const route = await fetchShipmentRouteById(shipmentRouteId, signal);
  if (!route || typeof route.shipmentId !== 'number') {
    return null;
  }

  const shipment = await fetchShipmentById(route.shipmentId, signal);
  if (!shipment) {
    return null;
  }

  const assignmentDetails = toAssignmentTypeAndCoordinates(route, shipment);
  if (!assignmentDetails) {
    return null;
  }

  return {
    assignmentId: assignment.id ?? null,
    shipmentRouteId,
    shipmentId: route.shipmentId,
    parcelNumber: shipment.parcelNumber?.trim() || '-',
    senderName: shipment.senderName?.trim() || '-',
    senderPhone: shipment.senderPhone?.trim() || '-',
    recipientName: shipment.recipientName?.trim() || '-',
    recipientPhone: shipment.recipientPhone?.trim() || '-',
    routeAddress: assignmentDetails.routeAddress,
    assignmentType: assignmentDetails.assignmentType,
    pickedUpForDelivery: Boolean(assignment.pickedUpForDelivery),
    failed: Boolean(assignment.failed),
    latitude: assignmentDetails.latitude,
    longitude: assignmentDetails.longitude,
  };
};

export const fetchCourierAllocationsForCurrentDay = async (signal?: AbortSignal): Promise<CourierAllocation[]> => {
  const response = await courierApiClient.api.findAllForCourierForCurrentDay({
    signal,
    format: 'json',
  });
  const assignments = Array.isArray(response.data) ? response.data : [];

  if (assignments.length === 0) {
    return [];
  }

  const hydratedAssignments = await Promise.all(
    assignments.map(async (assignment) => {
      try {
        return await hydrateAssignment(assignment, signal);
      } catch {
        return null;
      }
    }),
  );

  return hydratedAssignments.filter((allocation): allocation is CourierAllocation => allocation !== null);
};
