import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { LocationCountryDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link, useSearchParams } from 'react-router-dom';
import { getCountriesByRegionId, getRegionById } from '../../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../../navigation';
import { EntityListShell } from '../../shared/EntityListShell';
import { LanguageSwitcher } from '../../../i18n/LanguageSwitcher';

const parseSelectedId = (value: string | null) => {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
};

export const LogisticsCountriesPage = () => {
  const { t } = useTranslation('location');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const notAvailable = tCommon('status.notAvailable');

  const valueOrFallback = (value?: number | string) =>
    typeof value === 'number' || (typeof value === 'string' && value.length > 0) ? value : notAvailable;

  const [searchParams] = useSearchParams();
  const regionId = parseSelectedId(searchParams.get('regionId'));

  const countriesQuery = useQuery({
    queryKey: ['logistics', 'locations', 'countries', regionId],
    queryFn: () => getCountriesByRegionId(regionId as number),
    enabled: regionId !== null,
  });
  const regionQuery = useQuery({
    queryKey: ['logistics', 'locations', 'region', regionId],
    queryFn: () => getRegionById(regionId as number),
    enabled: regionId !== null,
  });

  const selectedRegionName = useMemo(() => {
    if (regionId === null) {
      return '';
    }

    if (regionQuery.isLoading) {
      return tCommon('status.loadingEllipsis');
    }

    // regionQuery.data.name backend-ről érkező szabad szöveg, nem fordítjuk.
    return regionQuery.data?.name ?? notAvailable;
  }, [regionId, regionQuery.data, regionQuery.isLoading, tCommon, notAvailable]);

  const columns = useMemo<DataTableColumn<LocationCountryDTO>[]>(
    () => [
      {
        id: 'name',
        header: t('country.list.columns.name'),
        mobileLabel: t('country.list.columns.name'),
        // country.name backend-ről érkező szabad szöveg, nem fordítjuk.
        cell: (country) => valueOrFallback(country.name),
      },
      {
        id: 'currency',
        header: t('country.list.columns.currency'),
        mobileLabel: t('country.list.columns.currency'),
        cell: (country) => valueOrFallback(country.currencyCode),
      },
      {
        id: 'next',
        header: t('country.list.columns.next'),
        cell: (country) =>
          typeof country.id === 'number' ? (
            <Link
              to={`/portal/locations/cities?countryId=${country.id}`}
              className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
            >
              {t('country.list.columns.goToCities')}
            </Link>
          ) : (
            <span className="text-on-surface-variant">{notAvailable}</span>
          ),
      },
      {
        id: 'edit',
        header: tCommon('table.editHeader'),
        cell: (country) =>
          typeof country.id === 'number' ? (
            <Link
              to={`/portal/locations/countries/${country.id}/edit${regionId !== null ? `?regionId=${regionId}` : ''}`}
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
    [regionId, t, tCommon],
  );

  return (
    <EntityListShell
      title={t('country.list.title')}
      activeHref="#/portal/locations/regions"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('country.list.eyebrow')}
      heading={t('country.list.heading')}
      contextInfo={
        regionId !== null ? (
          <p className="mt-2 font-body text-on-surface-variant">
            {t('country.list.selectedRegion', { regionName: selectedRegionName })}
          </p>
        ) : null
      }
      headerActions={
        <>
          <Link
            to="/portal/locations/regions"
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {t('country.list.backToRegions')}
          </Link>
          {regionId !== null ? (
            <Link
              to={`/portal/locations/countries/new?regionId=${regionId}`}
              className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
            >
              {t('country.list.addNew')}
            </Link>
          ) : null}
        </>
      }
      readyGuard={regionId !== null}
      emptyGuardMessage={t('country.list.emptyGuard')}
      isLoading={countriesQuery.isLoading}
      loadingMessage={t('country.list.loading')}
      isError={countriesQuery.isError}
      errorMessage={t('country.list.errorHeading')}
      errorDetail={(countriesQuery.error as Error)?.message}
      onRetry={() => {
        void countriesQuery.refetch();
      }}
    >
      <DataTable
        data={countriesQuery.data ?? []}
        rowKey={(country, index) => `country-${country.id ?? country.name ?? index}`}
        title={t('country.list.tableTitle')}
        columns={columns}
        emptyMessage={t('country.list.empty')}
        mobileCardEyebrow={t('country.list.mobileEyebrow')}
      />
    </EntityListShell>
  );
};
