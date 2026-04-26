import type { DepoWithLookups } from '../api/logisticsDeposApi';
import { Link } from 'react-router-dom';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn, DataTableFilter } from '@package/shared-ui';

type DeposDataTableProps = {
  depos: DepoWithLookups[];
};

const valueOrFallback = (value?: string | number) =>
  value === 0 || (typeof value === 'string' && value.length > 0) ? value : 'N/A';

export const DeposDataTable = ({ depos }: DeposDataTableProps) => {
  const columns: DataTableColumn<DepoWithLookups>[] = [
    {
      id: 'name',
      header: 'Név',
      mobileLabel: 'Név',
      cell:  (depo) => {
        if (typeof depo.id !== 'number') {
          return 'N/A';
        }

        const depoId = depo.id;
        const depoName = depo.name ?? `#${depoId}`;

        return (
            <Link to={`/portal/depos/${depoId}`} className="text-on-primary-container underline hover:text-primary-container">
              {depoName}
            </Link>
        );
      }
    },
    {
      id: 'country',
      header: 'Ország',
      filterId: 'country',
      mobileLabel: 'Ország',
      cell: (depo) => valueOrFallback(depo.countryName),
    },
    {
      id: 'city',
      header: 'Város',
      filterId: 'city',
      mobileLabel: 'Város',
      cell: (depo) => valueOrFallback(depo.cityName),
    },
    {
      id: 'zip',
      header: 'Irányítószám',
      mobileLabel: 'Irányítószám',
      cell: (depo) => valueOrFallback(depo.zip),
    },
    {
      id: 'address',
      header: 'Cím',
      mobileLabel: 'Cím',
      cell: (depo) => valueOrFallback(depo.address),
    },
    {
      id: 'edit',
      header: 'Szerkesztés',
      cell: (depo) =>
        typeof depo.id === 'number' ? (
          <Link
            to={`/portal/depos/${depo.id}/edit`}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Szerkeszt
          </Link>
        ) : (
          <span className="text-on-surface-variant">N/A</span>
        ),
    },
  ];

  const filters: DataTableFilter<DepoWithLookups>[] = [
    {
      id: 'country',
      label: 'Ország',
      allOptionLabel: 'Minden ország',
      getOptionValue: (depo) => depo.countryName,
    },
    {
      id: 'city',
      label: 'Város',
      allOptionLabel: 'Minden város',
      dependsOn: 'country',
      dependsOnText: 'Először válassz országot.',
      getOptionValue: (depo) => depo.cityName,
    },
  ];

  return (
    <DataTable
      data={depos}
      rowKey={(depo, index) => String(depo.id ?? `${depo.address ?? 'depo'}-${index}`)}
      title="Depo adatok"
      columns={columns}
      filters={filters}
      emptyMessage="Nincs találat a kiválasztott ország és város szűrőkre."
      mobileCardEyebrow="Depo"
      recordCountLabel={(visible, total) => `Megjelenített rekordok: ${visible} / ${total}`}
      renderMobileActions={(depo) =>
        typeof depo.id === 'number' ? (
          <>
            <Link
              to={`/portal/depos/${depo.id}`}
              className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-semibold text-on-primary"
            >
              Megnyit
            </Link>
            <Link
              to={`/portal/depos/${depo.id}/edit`}
              className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-semibold text-on-surface"
            >
              Szerkeszt
            </Link>
          </>
        ) : (
          <span className="text-on-surface-variant">N/A</span>
        )
      }
    />
  );
};

