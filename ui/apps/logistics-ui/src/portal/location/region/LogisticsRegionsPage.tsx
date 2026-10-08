import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { LocationRegionDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link } from 'react-router-dom';
import { getAllRegions } from '../../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../../navigation';
import { EntityListShell } from '../../shared/EntityListShell';
import { LanguageSwitcher } from '../../../i18n/LanguageSwitcher';

export const LogisticsRegionsPage = () => {
  const { t } = useTranslation('location');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const notAvailable = tCommon('status.notAvailable');

  const valueOrFallback = (value?: number | string) =>
    typeof value === 'number' || (typeof value === 'string' && value.length > 0) ? value : notAvailable;

  const regionsQuery = useQuery({
    queryKey: ['logistics', 'locations', 'regions'],
    queryFn: getAllRegions,
  });

  const columns = useMemo<DataTableColumn<LocationRegionDTO>[]>(
    () => [
      {
        id: 'name',
        header: t('region.list.columns.name'),
        mobileLabel: t('region.list.columns.name'),
        // region.name backend-ről érkező szabad szöveg, nem fordítjuk.
        cell: (region) => valueOrFallback(region.name),
      },
      {
        id: 'next',
        header: t('region.list.columns.next'),
        cell: (region) =>
          typeof region.id === 'number' ? (
            <Link
              to={`/portal/locations/countries?regionId=${region.id}`}
              className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
            >
              {t('region.list.columns.goToCountries')}
            </Link>
          ) : (
            <span className="text-on-surface-variant">{notAvailable}</span>
          ),
      },
      {
        id: 'edit',
        header: tCommon('table.editHeader'),
        cell: (region) =>
          typeof region.id === 'number' ? (
            <Link
              to={`/portal/locations/regions/${region.id}/edit`}
              className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
            >
              {tCommon('buttons.edit')}
            </Link>
          ) : (
            <span className="text-on-surface-variant">{notAvailable}</span>
          ),
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, tCommon],
  );

  return (
    <EntityListShell
      title={t('region.list.title')}
      activeHref="#/portal/locations/regions"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('region.list.eyebrow')}
      heading={t('region.list.heading')}
      headerActions={
        <Link
          to="/portal/locations/regions/new"
          className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
        >
          {t('region.list.addNew')}
        </Link>
      }
      isLoading={regionsQuery.isLoading}
      loadingMessage={t('region.list.loading')}
      isError={regionsQuery.isError}
      errorMessage={t('region.list.errorHeading')}
      errorDetail={(regionsQuery.error as Error)?.message}
      onRetry={() => {
        void regionsQuery.refetch();
      }}
    >
      <DataTable
        data={regionsQuery.data ?? []}
        rowKey={(region, index) => `region-${region.id ?? region.name ?? index}`}
        title={t('region.list.tableTitle')}
        columns={columns}
        emptyMessage={t('region.list.empty')}
        mobileCardEyebrow={t('region.list.mobileEyebrow')}
      />
    </EntityListShell>
  );
};
