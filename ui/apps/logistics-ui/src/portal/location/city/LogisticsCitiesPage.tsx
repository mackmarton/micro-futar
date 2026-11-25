import { useEffect, useMemo, useRef } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { DataTable, useAuth } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { LocationCityDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link, useSearchParams } from 'react-router-dom';
import {
  geocodeMissingCities,
  getCitiesByCountryId,
  getCountryById,
  getGeocodeMissingStatus,
} from '../../api/logisticsDeposApi';
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
  const queryClient = useQueryClient();
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');

  const geocodeStatusQuery = useQuery({
    queryKey: ['logistics', 'locations', 'geocode-missing-status'],
    queryFn: getGeocodeMissingStatus,
    enabled: isAdmin,
    // A run is in progress on the server: poll until it finishes.
    refetchInterval: (query) => (query.state.data?.state === 'RUNNING' ? 2000 : false),
  });
  const geocodeStatus = geocodeStatusQuery.data;
  const geocodeRunning = geocodeStatus?.state === 'RUNNING';

  const geocodeMutation = useMutation({
    mutationFn: geocodeMissingCities,
    onSuccess: (status) => {
      queryClient.setQueryData(['logistics', 'locations', 'geocode-missing-status'], status);
      void queryClient.invalidateQueries({ queryKey: ['logistics', 'locations', 'geocode-missing-status'] });
    },
  });

  // Refresh the list when a run completes so the new geo data shows up.
  const previousGeocodeState = useRef(geocodeStatus?.state);
  useEffect(() => {
    if (previousGeocodeState.current === 'RUNNING' && geocodeStatus?.state !== 'RUNNING') {
      void queryClient.invalidateQueries({ queryKey: ['logistics', 'locations', 'cities'] });
    }
    previousGeocodeState.current = geocodeStatus?.state;
  }, [geocodeStatus?.state, queryClient]);

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
        id: 'geo',
        header: t('city.list.columns.geo'),
        mobileLabel: t('city.list.columns.geo'),
        cell: (city) => (
          <span className="inline-flex flex-wrap items-center gap-2">
            <span
              className={
                typeof city.centerLatitude === 'number'
                  ? 'rounded-full bg-green-100 px-2 py-0.5 text-xs font-semibold text-green-800'
                  : 'rounded-full bg-amber-100 px-2 py-0.5 text-xs font-semibold text-amber-800'
              }
            >
              {typeof city.centerLatitude === 'number' ? t('city.list.geo.present') : t('city.list.geo.missing')}
            </span>
            {city.geoManual ? (
              <span className="rounded-full bg-blue-100 px-2 py-0.5 text-xs font-semibold text-blue-800">
                {t('city.list.geo.manual')}
              </span>
            ) : null}
          </span>
        ),
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
        <>
          {countryId !== null ? (
            <p className="mt-2 font-body text-on-surface-variant">
              {t('city.list.selectedCountry', { countryName: selectedCountryName })}
            </p>
          ) : null}
          {isAdmin && geocodeRunning ? (
            <p className="mt-2 font-body text-on-surface-variant" role="status">
              {t('city.list.geocodeMissing.running', {
                processed: geocodeStatus?.processed ?? 0,
                total: geocodeStatus?.total ?? 0,
              })}
            </p>
          ) : null}
          {isAdmin && geocodeStatus?.state === 'DONE' ? (
            <p className="mt-2 font-body text-on-surface-variant" role="status">
              {t('city.list.geocodeMissing.done', {
                geocoded: geocodeStatus.geocoded ?? 0,
                notFound: geocodeStatus.notFound ?? 0,
                failed: geocodeStatus.failed ?? 0,
              })}
            </p>
          ) : null}
          {isAdmin && geocodeStatus?.state === 'FAILED' ? (
            <p className="mt-2 font-body text-red-700" role="alert">
              {t('city.list.geocodeMissing.failed')}
            </p>
          ) : null}
          {isAdmin && geocodeMutation.isError ? (
            <p className="mt-2 font-body text-red-700" role="alert">
              {(geocodeMutation.error as Error)?.message || t('city.list.geocodeMissing.error')}
            </p>
          ) : null}
        </>
      }
      headerActions={
        <>
          <Link
            to={countriesPageHref}
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            {t('city.list.backToCountries')}
          </Link>
          {isAdmin ? (
            <button
              type="button"
              onClick={() => geocodeMutation.mutate()}
              disabled={geocodeRunning || geocodeMutation.isPending}
              className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container disabled:cursor-not-allowed disabled:opacity-70"
            >
              {geocodeRunning || geocodeMutation.isPending
                ? t('city.list.geocodeMissing.busy')
                : t('city.list.geocodeMissing.button')}
            </button>
          ) : null}
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
