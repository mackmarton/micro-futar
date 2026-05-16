import { resolveApiBaseUrl } from '@package/shared-core';
import {
  Api,
  type ShipmentRouteCourierDTO,
  type PackageSizeDTO,
  type ShipmentDTO,
  type ShipmentRouteDTO,
} from '@package/shared-core/api/CourierApiClient';

const courierApiClient = new Api({
  baseUrl: resolveApiBaseUrl(),
  baseApiParams: {
    credentials: 'include',
  },
});

export const fetchCourierPickupsForToday = async (signal?: AbortSignal): Promise<ShipmentRouteCourierDTO[]> => {
  const response = await courierApiClient.api.findAllForCourierForCurrentDay({
    signal,
    format: 'json',
  });

  return Array.isArray(response.data) ? response.data : [];
};

export const pickUpAllDeliveryShipmentsForCurrentDay = async (): Promise<void> => {
  await courierApiClient.api.pickUpAllDeliveryShipmentsForCurrentDay();
};

export const pickUpParcel = async (assignmentId: number): Promise<void> => {
  await courierApiClient.api.pickUpParcel(assignmentId);
};

export const fulfillShipmentRouteAssignment = async (assignmentId: number): Promise<void> => {
  await courierApiClient.api.fulfillShipmentRouteAssignment(assignmentId);
};

export const failShipmentRouteAssignment = async (assignmentId: number): Promise<void> => {
  await courierApiClient.api.failShipmentRouteAssignment(assignmentId);
};

export type ManifestShipment = {
  assignmentId: number | null;
  shipmentRouteId: number | null;
  assignmentType: 'Pickup' | 'Delivery' | '-';
  parcelNumber: string;
  contact: string;
  routeAddress: string;
  pickedUpForDelivery: boolean;
  failed: boolean;
  packageSize: string;
  status: string;
};

const getAssignmentStatusLabel = (assignment: ShipmentRouteCourierDTO, route?: ShipmentRouteDTO | null): string => {
  if (route?.fulfillmentTime) {
    return 'Depóban leadva';
  }

  if (assignment.failed) {
    return 'Sikertelen';
  }

  if (assignment.pickedUpForDelivery) {
    return 'Felvéve';
  }

  return 'Várakozik';
};

const toPackageSizeNameById = (packageSizes: PackageSizeDTO[]): Map<number, string> => {
  return new Map(
    packageSizes
      .filter((packageSize): packageSize is Required<Pick<PackageSizeDTO, 'id' | 'name'>> => {
        return typeof packageSize.id === 'number' && typeof packageSize.name === 'string';
      })
      .map((packageSize) => [packageSize.id, packageSize.name]),
  );
};

const isNonEmpty = (value: string | null | undefined): value is string => {
  return typeof value === 'string' && value.trim().length > 0;
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

export const fetchManifestShipmentsForAssignments = async (
  assignments: ShipmentRouteCourierDTO[],
  signal?: AbortSignal,
): Promise<ManifestShipment[]> => {
  if (assignments.length === 0) {
    return [];
  }

  const packageSizesResponse = await courierApiClient.api.findAll3({
    signal,
    format: 'json',
  });
  const packageSizes = Array.isArray(packageSizesResponse.data) ? packageSizesResponse.data : [];
  const packageSizeNameById = toPackageSizeNameById(packageSizes);

  const manifestRows = await Promise.all(
    assignments.map(async (assignment) => {
      const assignmentId = assignment.id ?? null;
      const shipmentRouteId = assignment.shipmentRouteId ?? null;
      const pickedUpForDelivery = Boolean(assignment.pickedUpForDelivery);
      const failed = Boolean(assignment.failed);

      if (!shipmentRouteId) {
        return {
          assignmentId,
          shipmentRouteId,
          assignmentType: '-',
          parcelNumber: '-',
          contact: '-',
          routeAddress: '-',
          pickedUpForDelivery,
          failed,
          packageSize: '-',
          status: getAssignmentStatusLabel(assignment),
        } satisfies ManifestShipment;
      }

      try {
        const route = await fetchShipmentRouteById(shipmentRouteId, signal);
        const status = getAssignmentStatusLabel(assignment, route);
        const shipmentId = route?.shipmentId;
        if (!shipmentId) {
          return {
            assignmentId,
            shipmentRouteId,
            assignmentType: '-',
            parcelNumber: '-',
            contact: '-',
            routeAddress: '-',
            pickedUpForDelivery,
            failed,
            packageSize: '-',
            status,
          } satisfies ManifestShipment;
        }

        const shipment = await fetchShipmentById(shipmentId, signal);
        const assignmentType = isNonEmpty(route.originAddress)
          ? 'Pickup'
          : isNonEmpty(route.destinationAddress)
            ? 'Delivery'
            : '-';
        const contact =
          assignmentType === 'Pickup'
            ? (shipment?.senderName?.trim() || '-')
            : assignmentType === 'Delivery'
              ? (shipment?.recipientName?.trim() || '-')
              : '-';
        const routeAddress =
          assignmentType === 'Pickup'
            ? (route.originAddress?.trim() || '-')
            : assignmentType === 'Delivery'
              ? (route.destinationAddress?.trim() || '-')
              : '-';
        const packageSizeName =
          typeof shipment?.packageSizeId === 'number'
            ? (packageSizeNameById.get(shipment.packageSizeId) ?? `#${shipment.packageSizeId}`)
            : '-';

        return {
          assignmentId,
          shipmentRouteId,
          assignmentType,
          parcelNumber: shipment?.parcelNumber?.trim() || '-',
          contact,
          routeAddress,
          pickedUpForDelivery,
          failed,
          packageSize: packageSizeName,
          status,
        } satisfies ManifestShipment;
      } catch {
        return {
          assignmentId,
          shipmentRouteId,
          assignmentType: '-',
          parcelNumber: '-',
          contact: '-',
          routeAddress: '-',
          pickedUpForDelivery,
          failed,
          packageSize: '-',
          status: getAssignmentStatusLabel(assignment),
        } satisfies ManifestShipment;
      }
    }),
  );

  return manifestRows;
};
