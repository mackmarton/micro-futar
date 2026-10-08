import type { DepoWithLookups } from '../../api/logisticsDeposApi';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn, DataTableFilter } from '@package/shared-ui';

type DeposDataTableProps = {
  depos: DepoWithLookups[];
};

const valueOrFallback = (value: string | number | undefined, notAvailableLabel: string) =>
  typeof value === 'number' || (typeof value === 'string' && value.length > 0) ? value : notAvailableLabel;

export const DeposDataTable = ({ depos }: DeposDataTableProps) => {
  const { t } = useTranslation('depo');
  const { t: tCommon } = useTranslation('common');
  const notAvailable = tCommon('status.notAvailable');

  const columns: DataTableColumn<DepoWithLookups>[] = [
    {
      id: 'name',
      header: t('table.columns.name'),
      mobileLabel: t('table.columns.name'),
      cell:  (depo) => {
        if (typeof depo.id !== 'number') {
          return notAvailable;
        }

        const depoId = depo.id;
        // A depó neve backend-ről érkező szabad szöveg, nem fordítjuk.
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
      header: t('table.columns.country'),
      filterId: 'country',
      mobileLabel: t('table.columns.country'),
      // depo.countryName backend-ről érkező szabad szöveg, nem fordítjuk.
      cell: (depo) => valueOrFallback(depo.countryName, notAvailable),
    },
    {
      id: 'city',
      header: t('table.columns.city'),
      filterId: 'city',
      mobileLabel: t('table.columns.city'),
      // depo.cityName backend-ről érkező szabad szöveg, nem fordítjuk.
      cell: (depo) => valueOrFallback(depo.cityName, notAvailable),
    },
    {
      id: 'zip',
      header: t('table.columns.zip'),
      mobileLabel: t('table.columns.zip'),
      cell: (depo) => valueOrFallback(depo.zip, notAvailable),
    },
    {
      id: 'address',
      header: t('table.columns.address'),
      mobileLabel: t('table.columns.address'),
      cell: (depo) => valueOrFallback(depo.address, notAvailable),
    },
    {
      id: 'edit',
      header: tCommon('table.editHeader'),
      cell: (depo) =>
        typeof depo.id === 'number' ? (
          <Link
            to={`/portal/depos/${depo.id}/edit`}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {tCommon('buttons.edit')}
          </Link>
        ) : (
          <span className="text-on-surface-variant">{notAvailable}</span>
        ),
    },
  ];

  const filters: DataTableFilter<DepoWithLookups>[] = [
    {
      id: 'country',
      label: t('table.filters.countryLabel'),
      allOptionLabel: t('table.filters.countryAllOption'),
      getOptionValue: (depo) => depo.countryName,
    },
    {
      id: 'city',
      label: t('table.filters.cityLabel'),
      allOptionLabel: t('table.filters.cityAllOption'),
      dependsOn: 'country',
      dependsOnText: t('table.filters.cityDependsOnText'),
      getOptionValue: (depo) => depo.cityName,
    },
  ];

  return (
    <DataTable
      data={depos}
      rowKey={(depo, index) => String(depo.id ?? `${depo.address ?? 'depo'}-${index}`)}
      title={t('table.title')}
      columns={columns}
      filters={filters}
      emptyMessage={t('table.empty')}
      mobileCardEyebrow={t('table.mobileEyebrow')}
      recordCountLabel={(visible, total) => tCommon('table.recordCount', { visible, total })}
      renderMobileActions={(depo) =>
        typeof depo.id === 'number' ? (
          <>
            <Link
              to={`/portal/depos/${depo.id}`}
              className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-semibold text-on-primary"
            >
              {tCommon('buttons.open')}
            </Link>
            <Link
              to={`/portal/depos/${depo.id}/edit`}
              className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-semibold text-on-surface"
            >
              {tCommon('buttons.edit')}
            </Link>
          </>
        ) : (
          <span className="text-on-surface-variant">{notAvailable}</span>
        )
      }
    />
  );
};
