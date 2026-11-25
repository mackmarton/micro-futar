import { gatewayApiConfig } from '@package/shared-core';
import {
  Api,
  type MyAssignmentDTO,
  type PackageSizeDTO,
  type ShipmentRouteCourierDTO,
} from '@package/shared-core/api/CourierApiClient';
import { fetchMyAssignments } from '../../myAssignmentsApi.ts';

const courierApiClient = new Api(gatewayApiConfig());

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

// A státuszt egy stabil kulcsként adjuk vissza (nem kész szövegként), hogy a UI réteg
// (ManifestDataTable) tudja a jelenlegi nyelven megjeleníteni - lásd manifest.json "status" ns.
export type ManifestAssignmentStatus = 'droppedAtDepot' | 'failed' | 'pickedUp' | 'waiting';

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
  status: ManifestAssignmentStatus;
};

const getAssignmentStatus = (
  assignment: ShipmentRouteCourierDTO,
  details?: MyAssignmentDTO,
): ManifestAssignmentStatus => {
  if (details?.fulfillmentTime) {
    return 'droppedAtDepot';
  }

  if (assignment.failed) {
    return 'failed';
  }

  if (assignment.pickedUpForDelivery) {
    return 'pickedUp';
  }

  return 'waiting';
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

const toAssignmentTypeLabel = (assignmentType: MyAssignmentDTO['assignmentType']): ManifestShipment['assignmentType'] => {
  if (assignmentType === 'PICKUP') {
    return 'Pickup';
  }

  return assignmentType === 'DELIVERY' ? 'Delivery' : '-';
};

export const fetchManifestShipmentsForAssignments = async (
  assignments: ShipmentRouteCourierDTO[],
  signal?: AbortSignal,
): Promise<ManifestShipment[]> => {
  if (assignments.length === 0) {
    return [];
  }

  const [packageSizesResponse, myAssignments] = await Promise.all([
    courierApiClient.api.findAll3({
      signal,
      format: 'json',
    }),
    fetchMyAssignments(signal),
  ]);
  const packageSizes = Array.isArray(packageSizesResponse.data) ? packageSizesResponse.data : [];
  const packageSizeNameById = toPackageSizeNameById(packageSizes);
  const detailsByAssignmentId = new Map(myAssignments.map((details) => [details.assignmentId, details]));

  return assignments.map((assignment) => {
    const details = detailsByAssignmentId.get(assignment.id);
    const packageSizeId = details?.packageSizeId;

    return {
      assignmentId: assignment.id ?? null,
      shipmentRouteId: assignment.shipmentRouteId ?? null,
      assignmentType: toAssignmentTypeLabel(details?.assignmentType),
      parcelNumber: details?.parcelNumber?.trim() || '-',
      contact: details?.contactName?.trim() || '-',
      routeAddress: details?.address?.trim() || '-',
      pickedUpForDelivery: Boolean(assignment.pickedUpForDelivery),
      failed: Boolean(assignment.failed),
      packageSize:
        typeof packageSizeId === 'number'
          ? (packageSizeNameById.get(packageSizeId) ?? `#${packageSizeId}`)
          : '-',
      status: getAssignmentStatus(assignment, details),
    } satisfies ManifestShipment;
  });
};
