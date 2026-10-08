import { useCallback, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import { Link } from 'react-router-dom';
import type { CourierAllocation } from '../api/courierAllocationsApi.ts';

type CourierAllocationsListProps = {
  allocations: CourierAllocation[];
  isLoading: boolean;
};

const typeBadgeClassNameByType: Record<CourierAllocation['assignmentType'], string> = {
  Pickup: 'bg-primary text-on-primary',
  Delivery: 'bg-tertiary text-on-tertiary',
};

export const CourierAllocationsList = ({ allocations, isLoading }: CourierAllocationsListProps) => {
  const { t } = useTranslation(['allocations', 'common']);

  const assignmentTypeLabel = useCallback(
    (assignmentType: CourierAllocation['assignmentType']) =>
      t(`common:assignmentType.${assignmentType === 'Pickup' ? 'pickup' : 'delivery'}`),
    [t],
  );

  const columns = useMemo<DataTableColumn<CourierAllocation>[]>(
    () => [
      {
        id: 'assignmentType',
        header: t('allocations:table.columns.type'),
        mobileLabel: t('allocations:table.columns.type'),
        cell: (allocation) => (
          <span
            className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide ${typeBadgeClassNameByType[allocation.assignmentType]}`}
          >
            {assignmentTypeLabel(allocation.assignmentType)}
          </span>
        ),
      },
      {
        id: 'parcelNumber',
        header: t('allocations:table.columns.parcelNumber'),
        mobileLabel: t('allocations:table.columns.parcelNumber'),
        // allocation.parcelNumber a shipment DTO-ból jön, szabadszöveges adat - nem fordítjuk.
        cell: (allocation) => allocation.parcelNumber,
      },
      {
        id: 'contactName',
        header: t('allocations:table.columns.contact'),
        mobileLabel: t('allocations:table.columns.contact'),
        // sender/recipient név a backendről jön, szabadszöveges adat - nem fordítjuk.
        cell: (allocation) => (allocation.assignmentType === 'Pickup' ? allocation.senderName : allocation.recipientName),
      },
      {
        id: 'routeAddress',
        header: t('allocations:table.columns.address'),
        mobileLabel: t('allocations:table.columns.address'),
        // allocation.routeAddress a backendről jön, szabadszöveges adat - nem fordítjuk.
        cell: (allocation) => allocation.routeAddress,
      },
      {
        id: 'action',
        header: t('allocations:table.columns.action'),
        cell: (allocation) =>
          typeof allocation.assignmentId === 'number' ? (
            <Link
              to={`/portal/allocated-packages/${allocation.assignmentId}`}
              className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-medium text-on-primary transition-all duration-200 hover:bg-[linear-gradient(95deg,#000000_0%,#0c9488_100%)]"
            >
              {t('common:actions.open')}
            </Link>
          ) : (
            <span className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-medium text-on-surface-variant">
              {t('common:status.notAvailable')}
            </span>
          ),
      },
    ],
    [t, assignmentTypeLabel],
  );

  return (
    <DataTable
      data={allocations}
      rowKey={(allocation, index) =>
        `${allocation.assignmentId ?? allocation.shipmentRouteId ?? allocation.shipmentId ?? 'allocation'}-${index}`
      }
      title={t('allocations:table.title')}
      columns={columns}
      emptyMessage={
        isLoading
          ? t('allocations:table.emptyMessage.loading')
          : t('allocations:table.emptyMessage.empty')
      }
      mobileCardEyebrow={t('allocations:table.mobileCardEyebrow')}
      recordCountLabel={(visible, total) =>
        isLoading ? t('allocations:table.recordCountLabel.loading') : t('allocations:table.recordCountLabel.loaded', { visible, total })
      }
      renderMobileActions={(allocation) =>
        typeof allocation.assignmentId === 'number' ? (
          <Link
            to={`/portal/allocated-packages/${allocation.assignmentId}`}
            className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-medium text-on-primary"
          >
            {t('common:actions.open')}
          </Link>
        ) : null
      }
    />
  );
};
