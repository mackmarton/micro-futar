import { useCallback, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import { toErrorMessage } from '@package/shared-core';
import type { ShipmentRouteCourierDTO } from '@package/shared-core/api/CourierApiClient';
import {
  fetchManifestShipmentsForAssignments,
  type ManifestShipment,
} from '../api/courierPickupApi.ts';

type ManifestDataTableProps = {
  assignments: ShipmentRouteCourierDTO[];
};

export const ManifestDataTable = ({ assignments }: ManifestDataTableProps) => {
  const { t } = useTranslation(['manifest', 'common']);

  const assignmentTypeLabel = useCallback(
    (assignmentType: ManifestShipment['assignmentType']) =>
      assignmentType === '-' ? '-' : t(`common:assignmentType.${assignmentType === 'Pickup' ? 'pickup' : 'delivery'}`),
    [t],
  );

  const columns = useMemo<DataTableColumn<ManifestShipment>[]>(
    () => [
      {
        id: 'assignmentType',
        header: t('manifest:columns.type'),
        mobileLabel: t('manifest:columns.type'),
        cell: (shipment) => (
          <span
            className={
              shipment.assignmentType === 'Pickup'
                ? 'inline-flex rounded-full bg-primary px-3 py-1 text-xs font-semibold uppercase tracking-wide text-on-primary'
                : shipment.assignmentType === 'Delivery'
                  ? 'inline-flex rounded-full bg-tertiary px-3 py-1 text-xs font-semibold uppercase tracking-wide text-on-tertiary'
                  : 'inline-flex rounded-full bg-surface px-3 py-1 text-xs font-semibold uppercase tracking-wide text-on-surface'
            }
          >
            {assignmentTypeLabel(shipment.assignmentType)}
          </span>
        ),
      },
      {
        id: 'parcelNumber',
        header: t('manifest:columns.parcelNumber'),
        mobileLabel: t('manifest:columns.parcelNumber'),
        // shipment.parcelNumber a backendtől (shipment DTO) jön, szabadszöveges adat - nem fordítjuk.
        cell: (shipment) => shipment.parcelNumber,
      },
      {
        id: 'contact',
        header: t('manifest:columns.contact'),
        mobileLabel: t('manifest:columns.contact'),
        // shipment.contact a feladó/címzett neve a backendből - szabadszöveges adat, nem fordítjuk.
        cell: (shipment) => shipment.contact,
      },
      {
        id: 'packageSize',
        header: t('manifest:columns.packageSize'),
        mobileLabel: t('manifest:columns.packageSize'),
        // shipment.packageSize a logisztikai szolgáltatás által karbantartott csomagméret neve -
        // szabadszöveges adat a backendből, nem fordítjuk.
        cell: (shipment) => shipment.packageSize,
      },
      {
        id: 'status',
        header: t('manifest:columns.status'),
        mobileLabel: t('manifest:columns.status'),
        cell: (shipment) => t(`manifest:status.${shipment.status}`),
      },
    ],
    [t, assignmentTypeLabel],
  );

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
  const errorMessage = manifestQuery.isError
    ? toErrorMessage(manifestQuery.error, t('manifest:loadError'))
    : null;

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
          {t('common:actions.retry')}
        </button>
      </section>
    );
  }

  return (
    <DataTable
      data={shipments}
      rowKey={(shipment, index) => `${shipment.assignmentId ?? shipment.shipmentRouteId ?? 'row'}-${index}`}
      title={t('manifest:title')}
      columns={columns}
      emptyMessage={
        assignments.length === 0
          ? t('manifest:emptyMessage.noAssignments')
          : t('manifest:emptyMessage.noMatches')
      }
      mobileCardEyebrow={t('manifest:mobileCardEyebrow')}
      recordCountLabel={(visible, total) => t('manifest:recordCountLabel', { visible, total })}
    />
  );
};
