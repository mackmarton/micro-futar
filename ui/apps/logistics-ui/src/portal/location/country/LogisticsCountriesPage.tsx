import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { DataTable, PortalLayout } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { LocationCountryDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link, useSearchParams } from 'react-router-dom';
import { getCountriesByRegionId, getRegionById } from '../../api/logisticsDeposApi';
import { logisticsNavigationItems } from '../../navigation';

const valueOrFallback = (value?: number | string) =>
  value === 0 || (typeof value === 'string' && value.length > 0) ? value : 'N/A';

const parseSelectedId = (value: string | null) => {
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
};

export const LogisticsCountriesPage = () => {
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
      return 'Betöltés...';
    }

    return regionQuery.data?.name ?? 'N/A';
  }, [regionId, regionQuery.data, regionQuery.isLoading]);

  const columns = useMemo<DataTableColumn<LocationCountryDTO>[]>(
    () => [
      {
        id: 'name',
        header: 'Ország',
        mobileLabel: 'Ország',
        cell: (country) => valueOrFallback(country.name),
      },
      {
        id: 'next',
        header: 'Tovább',
        cell: (country) =>
          typeof country.id === 'number' ? (
            <Link
              to={`/portal/locations/cities?countryId=${country.id}`}
              className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
            >
              Városok
            </Link>
          ) : (
            <span className="text-on-surface-variant">N/A</span>
          ),
      },
      {
        id: 'edit',
        header: 'Szerkesztés',
        cell: (country) =>
          typeof country.id === 'number' ? (
            <Link
              to={`/portal/locations/countries/${country.id}/edit${regionId !== null ? `?regionId=${regionId}` : ''}`}
              className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
            >
              Szerkeszt
            </Link>
          ) : (
            <span className="text-on-surface-variant">N/A</span>
          ),
      },
    ],
    [regionId],
  );

  return (
    <PortalLayout title="Országok" activeHref="#/portal/locations/regions" navigationItems={logisticsNavigationItems}>
      <section className="rounded-2xl bg-surface-container-low p-6">
        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Helyszínek</p>
        <h1 className="mt-2 text-2xl font-headline text-on-surface">Országok</h1>
        {regionId !== null ? (
          <p className="mt-2 font-body text-on-surface-variant">
            Kiválasztott régió: <span className="font-semibold text-on-surface">{selectedRegionName}</span>
          </p>
        ) : null}
        <div className="mt-4 flex flex-wrap gap-3">
          <Link
            to="/portal/locations/regions"
            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
          >
            Vissza a régiókhoz
          </Link>
          {regionId !== null ? (
            <Link
              to={`/portal/locations/countries/new?regionId=${regionId}`}
              className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
            >
              Új ország létrehozása
            </Link>
          ) : null}
        </div>
      </section>

      {regionId === null ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
          <p className="font-body text-on-surface">Válassz régiót a régió oldalon az országok listázásához.</p>
        </section>
      ) : null}

      {regionId !== null && countriesQuery.isLoading ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
          <p className="font-body text-on-surface">Országok betöltése folyamatban...</p>
        </section>
      ) : null}

      {regionId !== null && countriesQuery.isError ? (
        <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
          <p className="font-body text-on-surface">Nem sikerült betölteni az országokat.</p>
          <p className="mt-1 font-body text-on-surface-variant">
            {(countriesQuery.error as Error)?.message ?? 'Ismeretlen hiba'}
          </p>
          <button
            type="button"
            onClick={() => {
              void countriesQuery.refetch();
            }}
            className="mt-3 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
          >
            Ujrapróbálás
          </button>
        </section>
      ) : null}

      {regionId !== null && !countriesQuery.isLoading && !countriesQuery.isError ? (
        <div className="mt-6">
          <DataTable
            data={countriesQuery.data ?? []}
            rowKey={(country, index) => `country-${country.id ?? country.name ?? index}`}
            title="Ország lista"
            columns={columns}
            emptyMessage="A kiválasztott régióhoz nem tartozik ország."
            mobileCardEyebrow="Ország"
          />
        </div>
      ) : null}
    </PortalLayout>
  );
};
