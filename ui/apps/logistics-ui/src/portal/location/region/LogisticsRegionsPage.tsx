import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { LocationRegionDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link } from 'react-router-dom';
import { getAllRegions } from '../../api/logisticsDeposApi';
import { logisticsNavigationItems } from '../../navigation';
import { EntityListShell } from '../../shared/EntityListShell';

const valueOrFallback = (value?: number | string) =>
  value === 0 || (typeof value === 'string' && value.length > 0) ? value : 'N/A';

export const LogisticsRegionsPage = () => {
  const regionsQuery = useQuery({
    queryKey: ['logistics', 'locations', 'regions'],
    queryFn: getAllRegions,
  });

  const columns = useMemo<DataTableColumn<LocationRegionDTO>[]>(
    () => [
      {
        id: 'name',
        header: 'Régió',
        mobileLabel: 'Régió',
        cell: (region) => valueOrFallback(region.name),
      },
      {
        id: 'next',
        header: 'Tovább',
        cell: (region) =>
          typeof region.id === 'number' ? (
            <Link
              to={`/portal/locations/countries?regionId=${region.id}`}
              className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
            >
              Országok
            </Link>
          ) : (
            <span className="text-on-surface-variant">N/A</span>
          ),
      },
      {
        id: 'edit',
        header: 'Szerkesztés',
        cell: (region) =>
          typeof region.id === 'number' ? (
            <Link
              to={`/portal/locations/regions/${region.id}/edit`}
              className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
            >
              Szerkeszt
            </Link>
          ) : (
            <span className="text-on-surface-variant">N/A</span>
          ),
      },
    ],
    [],
  );

  return (
    <EntityListShell
      title="Régiók"
      activeHref="#/portal/locations/regions"
      navigationItems={logisticsNavigationItems}
      eyebrow="Helyszínek"
      heading="Régiók"
      headerActions={
        <Link
          to="/portal/locations/regions/new"
          className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
        >
          Új régió létrehozása
        </Link>
      }
      isLoading={regionsQuery.isLoading}
      loadingMessage="Régiók betöltése folyamatban..."
      isError={regionsQuery.isError}
      errorMessage="Nem sikerült betölteni a régiókat."
      errorDetail={(regionsQuery.error as Error)?.message}
      onRetry={() => {
        void regionsQuery.refetch();
      }}
    >
      <DataTable
        data={regionsQuery.data ?? []}
        rowKey={(region, index) => `region-${region.id ?? region.name ?? index}`}
        title="Régió lista"
        columns={columns}
        emptyMessage="Nincs elérhető régió."
        mobileCardEyebrow="Régió"
      />
    </EntityListShell>
  );
};
