import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { DataTable, PortalLayout } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { LocationCityDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link, useSearchParams } from 'react-router-dom';
import { getCitiesByCountryId, getCountryById } from './api/logisticsDeposApi';
import { logisticsNavigationItems } from './navigation';

const valueOrFallback = (value?: number | string) =>
  value === 0 || (typeof value === 'string' && value.length > 0) ? value : 'N/A';

const parseSelectedId = (value: string | null) => {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
};

export const LogisticsCitiesPage = () => {
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
      return 'Betöltés...';
    }

    return countryQuery.data?.name ?? 'N/A';
  }, [countryId, countryQuery.data, countryQuery.isLoading]);
  const countriesPageHref = useMemo(() => {
    const regionId = countryQuery.data?.regionId;
    return typeof regionId === 'number' ? `/portal/locations/countries?regionId=${regionId}` : '/portal/locations/countries';
  }, [countryQuery.data?.regionId]);

  const columns = useMemo<DataTableColumn<LocationCityDTO>[]>(
    () => [
      {
        id: 'id',
        header: 'ID',
        mobileLabel: 'ID',
        cell: (city) => city.id,
      },
      {
        id: 'name',
        header: 'Város',
        mobileLabel: 'Város',
        cell: (city) => valueOrFallback(city.name),
      }
    ],
    [],
  );

  return (
    <PortalLayout title="Városok" activeHref="#/portal/locations/regions" navigationItems={logisticsNavigationItems}>
      <section className="rounded-2xl bg-surface-container-low p-6">
        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Helyszínek</p>
        <h1 className="mt-2 text-2xl font-headline text-on-surface">Városok</h1>
        {countryId !== null ? (
          <p className="mt-2 font-body text-on-surface-variant">
            Kiválasztott ország: <span className="font-semibold text-on-surface">{selectedCountryName}</span>
          </p>
        ) : null}
      </section>

      <div className="mt-6">
        <Link
          to={countriesPageHref}
          className="inline-flex items-center rounded-lg bg-surface-container-low px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
        >
          Vissza az országokhoz
        </Link>
      </div>

      {countryId === null ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
          <p className="font-body text-on-surface">Válassz országot az országok oldalon a városok listázásához.</p>
        </section>
      ) : null}

      {countryId !== null && citiesQuery.isLoading ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
          <p className="font-body text-on-surface">Városok betöltése folyamatban...</p>
        </section>
      ) : null}

      {countryId !== null && citiesQuery.isError ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
          <p className="font-body text-on-surface">Nem sikerült betölteni a városokat.</p>
          <p className="mt-1 font-body text-on-surface-variant">
            {(citiesQuery.error as Error)?.message ?? 'Ismeretlen hiba'}
          </p>
          <button
            type="button"
            onClick={() => {
              void citiesQuery.refetch();
            }}
            className="mt-3 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
          >
            Ujrapróbálás
          </button>
        </section>
      ) : null}

      {countryId !== null && !citiesQuery.isLoading && !citiesQuery.isError ? (
        <div className="mt-6">
          <DataTable
            data={citiesQuery.data ?? []}
            rowKey={(city, index) => `city-${city.id ?? city.name ?? index}`}
            title="Város lista"
            columns={columns}
            emptyMessage="A kiválasztott országhoz nem tartozik város."
            mobileCardEyebrow="Város"
          />
        </div>
      ) : null}
    </PortalLayout>
  );
};
