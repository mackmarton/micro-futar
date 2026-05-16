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

const columns: DataTableColumn<CourierAllocation>[] = [
  {
    id: 'assignmentType',
    header: 'Típus',
    mobileLabel: 'Típus',
    cell: (allocation) => (
      <span
        className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide ${typeBadgeClassNameByType[allocation.assignmentType]}`}
      >
        {allocation.assignmentType}
      </span>
    ),
  },
  {
    id: 'parcelNumber',
    header: 'Csomagszám',
    mobileLabel: 'Csomagszám',
    cell: (allocation) => allocation.parcelNumber,
  },
  {
    id: 'contactName',
    header: 'Kontakt',
    mobileLabel: 'Kontakt',
    cell: (allocation) => (allocation.assignmentType === 'Pickup' ? allocation.senderName : allocation.recipientName),
  },
  {
    id: 'routeAddress',
    header: 'Cím',
    mobileLabel: 'Cím',
    cell: (allocation) => allocation.routeAddress,
  },
  {
    id: 'action',
    header: 'Művelet',
    cell: (allocation) =>
      typeof allocation.assignmentId === 'number' ? (
        <Link
          to={`/portal/allocated-packages/${allocation.assignmentId}`}
          className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-medium text-on-primary transition-all duration-200 hover:bg-[linear-gradient(95deg,#000000_0%,#0c9488_100%)]"
        >
          Megnyitás
        </Link>
      ) : (
        <span className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-medium text-on-surface-variant">
          N/A
        </span>
      ),
  },
];

export const CourierAllocationsList = ({ allocations, isLoading }: CourierAllocationsListProps) => {
  return (
    <DataTable
      data={allocations}
      rowKey={(allocation, index) =>
        `${allocation.assignmentId ?? allocation.shipmentRouteId ?? allocation.shipmentId ?? 'allocation'}-${index}`
      }
      title="Kiosztott csomagok"
      columns={columns}
      emptyMessage={
        isLoading
          ? 'A mai kiosztások betöltése folyamatban...'
          : 'A mai napra nincs megjeleníthető pickup vagy delivery kiosztás.'
      }
      mobileCardEyebrow="Kiosztás"
      recordCountLabel={(visible, total) =>
        isLoading ? 'Betöltés folyamatban...' : `Megjelenített kiosztások: ${visible} / ${total}`
      }
      renderMobileActions={(allocation) =>
        typeof allocation.assignmentId === 'number' ? (
          <Link
            to={`/portal/allocated-packages/${allocation.assignmentId}`}
            className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-medium text-on-primary"
          >
            Megnyitás
          </Link>
        ) : null
      }
    />
  );
};
