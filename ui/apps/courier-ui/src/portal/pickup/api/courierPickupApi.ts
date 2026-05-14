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

export const pickUpAllShipmentsForCurrentDay = async (): Promise<void> => {
  await courierApiClient.api.pickUpAllShipmentsForCurrentDay();
};

export type ManifestShipment = {
  assignmentId: number | null;
  shipmentRouteId: number | null;
  parcelNumber: string;
  recipient: string;
  packageSize: string;
  status: string;
};

const getAssignmentStatusLabel = (assignment: ShipmentRouteCourierDTO): string => {
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
      const status = getAssignmentStatusLabel(assignment);

      if (!shipmentRouteId) {
        return {
          assignmentId,
          shipmentRouteId,
          parcelNumber: '-',
          recipient: '-',
          packageSize: '-',
          status,
        } satisfies ManifestShipment;
      }

      try {
        const route = await fetchShipmentRouteById(shipmentRouteId, signal);
        const shipmentId = route?.shipmentId;
        if (!shipmentId) {
          return {
            assignmentId,
            shipmentRouteId,
            parcelNumber: '-',
            recipient: '-',
            packageSize: '-',
            status,
          } satisfies ManifestShipment;
        }

        const shipment = await fetchShipmentById(shipmentId, signal);
        const packageSizeName =
          typeof shipment?.packageSizeId === 'number'
            ? (packageSizeNameById.get(shipment.packageSizeId) ?? `#${shipment.packageSizeId}`)
            : '-';

        return {
          assignmentId,
          shipmentRouteId,
          parcelNumber: shipment?.parcelNumber?.trim() || '-',
          recipient: shipment?.recipientName?.trim() || '-',
          packageSize: packageSizeName,
          status,
        } satisfies ManifestShipment;
      } catch {
        return {
          assignmentId,
          shipmentRouteId,
          parcelNumber: '-',
          recipient: '-',
          packageSize: '-',
          status,
        } satisfies ManifestShipment;
      }
    }),
  );

  return manifestRows;
};
