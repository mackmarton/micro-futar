import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { ShipmentRouteCourierDTO } from '@package/shared-core/api/CourierApiClient';
import {
  fetchManifestShipmentsForAssignments,
  type ManifestShipment,
} from '../api/courierPickupApi.ts';

type ManifestDataTableProps = {
  assignments: ShipmentRouteCourierDTO[];
};

const columns: DataTableColumn<ManifestShipment>[] = [
  {
    id: 'parcelNumber',
    header: 'Csomagszám',
    mobileLabel: 'Csomagszám',
    cell: (shipment) => shipment.parcelNumber,
  },
  {
    id: 'recipient',
    header: 'Címzett',
    mobileLabel: 'Címzett',
    cell: (shipment) => shipment.recipient,
  },
  {
    id: 'packageSize',
    header: 'Csomagméret',
    mobileLabel: 'Csomagméret',
    cell: (shipment) => shipment.packageSize,
  },
  {
    id: 'status',
    header: 'Státusz',
    mobileLabel: 'Státusz',
    cell: (shipment) => shipment.status,
  },
  {
    id: 'action',
    header: 'Művelet',
    cell: () => (
      <button
        type="button"
        className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-medium text-on-primary transition-all duration-200 hover:bg-[linear-gradient(95deg,#000000_0%,#0c9488_100%)]"
      >
        Megnyitás
      </button>
    ),
  },
];

const toErrorMessage = (error: unknown): string => {
  if (typeof error === 'object' && error !== null && 'error' in error) {
    const responseError = (error as { error?: { message?: string } }).error?.message;
    if (responseError) {
      return responseError;
    }
  }

  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message;
  }

  return 'Nem sikerult betolteni a szallitasi jegyzeket. Probald ujra.';
};

export const ManifestDataTable = ({ assignments }: ManifestDataTableProps) => {
  const queryKeySuffix = useMemo(
    () =>
      assignments.map((assignment, index) => ({
        id: assignment.id ?? index,
        shipmentRouteId: assignment.shipmentRouteId ?? null,
        pickedUpForDelivery: Boolean(assignment.pickedUpForDelivery),
        failed: Boolean(assignment.failed),
      })),
    [assignments],
  );

  const manifestQuery = useQuery({
    queryKey: ['courier-manifest-shipments', queryKeySuffix],
    queryFn: ({ signal }) => fetchManifestShipmentsForAssignments(assignments, signal),
    enabled: assignments.length > 0,
    retry: 1,
  });

  const shipments = manifestQuery.data ?? [];
  const errorMessage = manifestQuery.isError ? toErrorMessage(manifestQuery.error) : null;

  if (errorMessage) {
    return (
      <section className="rounded-xl bg-surface-container-low p-6 md:p-7">
        <p className="font-body text-sm text-red-600">{errorMessage}</p>
        <button
          type="button"
          onClick={() => {
            void manifestQuery.refetch();
          }}
          className="mt-4 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-medium text-on-primary"
        >
          Ujratoltes
        </button>
      </section>
    );
  }

  return (
    <DataTable
      data={shipments}
      rowKey={(shipment, index) => `${shipment.assignmentId ?? shipment.shipmentRouteId ?? 'row'}-${index}`}
      title="Szállítási jegyzék"
      columns={columns}
      emptyMessage={
        assignments.length === 0
          ? 'Nincs mai depó átvételi hozzárendelés.'
          : 'Nincs küldemény a megadott feltételek alapján.'
      }
      mobileCardEyebrow="Küldemény"
      recordCountLabel={(visible, total) => `Látható küldemények: ${visible} / ${total}`}
      renderMobileActions={() => (
        <button
          type="button"
          className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-medium text-on-primary"
        >
          Megnyitás
        </button>
      )}
    />
  );
};
