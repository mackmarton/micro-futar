import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { LocationCityDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link, useSearchParams } from 'react-router-dom';
import { getCitiesByCountryId, getCountryById } from '../../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../../navigation';
import { EntityListShell } from '../../shared/EntityListShell';
import { LanguageSwitcher } from '../../../i18n/LanguageSwitcher';

const parseSelectedId = (value: string | null) => {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
};

export const LogisticsCitiesPage = () => {
  const { t } = useTranslation('location');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const notAvailable = tCommon('status.notAvailable');

  const valueOrFallback = (value?: number | string) =>
    typeof value === 'number' || (typeof value === 'string' && value.length > 0) ? value : notAvailable;

  const [searchParams] = useSearchParams();
  const countryId = parseSelectedId(searchParams.get('countryId'));

  const citiesQuery = useQuery({
    queryKey: ['logistics', 'locations', 'cities', countryId],
    queryFn: () => getCitiesByCountryId(countryId as number),
    enabled: countryId !== null,
  });
  const countryQuery = useQuery({
    queryKey: ['logistics', 'locations', 'country', countryId],
    queryFn: () => getCountryById(countryId as number),
    enabled: countryId !== null,
  });

  const selectedCountryName = useMemo(() => {
    if (countryId === null) {
      return '';
    }

    if (countryQuery.isLoading) {
      return tCommon('status.loadingEllipsis');
    }

    // countryQuery.data.name backend-ről érkező szabad szöveg, nem fordítjuk.
    return countryQuery.data?.name ?? notAvailable;
  }, [countryId, countryQuery.data, countryQuery.isLoading, tCommon, notAvailable]);
  const countriesPageHref = useMemo(() => {
    const regionId = countryQuery.data?.regionId;
    return typeof regionId === 'number' ? `/portal/locations/countries?regionId=${regionId}` : '/portal/locations/countries';
  }, [countryQuery.data?.regionId]);

  const columns = useMemo<DataTableColumn<LocationCityDTO>[]>(
    () => [
      {
        id: 'id',
        header: t('city.list.columns.id'),
        mobileLabel: t('city.list.columns.id'),
        cell: (city) => city.id,
      },
      {
        id: 'name',
        header: t('city.list.columns.name'),
        mobileLabel: t('city.list.columns.name'),
        // city.name backend-ről érkező szabad szöveg, nem fordítjuk.
        cell: (city) => valueOrFallback(city.name),
      },
      {
        id: 'edit',
        header: tCommon('table.editHeader'),
        cell: (city) =>
          typeof city.id === 'number' ? (
            <Link
              to={`/portal/locations/cities/${city.id}/edit${countryId !== null ? `?countryId=${countryId}` : ''}`}
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
    [countryId, t, tCommon],
  );

  return (
    <EntityListShell
      title={t('city.list.title')}
      activeHref="#/portal/locations/regions"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('city.list.eyebrow')}
      heading={t('city.list.heading')}
      contextInfo={
        countryId !== null ? (
          <p className="mt-2 font-body text-on-surface-variant">
            {t('city.list.selectedCountry', { countryName: selectedCountryName })}
          </p>
        ) : null
      }
      headerActions={
        <>
          <Link
            to={countriesPageHref}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {t('city.list.backToCountries')}
          </Link>
          {countryId !== null ? (
            <Link
              to={`/portal/locations/cities/new?countryId=${countryId}`}
              className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
            >
              {t('city.list.addNew')}
            </Link>
          ) : null}
        </>
      }
      readyGuard={countryId !== null}
      emptyGuardMessage={t('city.list.emptyGuard')}
      isLoading={citiesQuery.isLoading}
      loadingMessage={t('city.list.loading')}
      isError={citiesQuery.isError}
      errorMessage={t('city.list.errorHeading')}
      errorDetail={(citiesQuery.error as Error)?.message}
      onRetry={() => {
        void citiesQuery.refetch();
      }}
    >
      <DataTable
        data={citiesQuery.data ?? []}
        rowKey={(city, index) => `city-${city.id ?? city.name ?? index}`}
        title={t('city.list.tableTitle')}
        columns={columns}
        emptyMessage={t('city.list.empty')}
        mobileCardEyebrow={t('city.list.mobileEyebrow')}
      />
    </EntityListShell>
  );
};
