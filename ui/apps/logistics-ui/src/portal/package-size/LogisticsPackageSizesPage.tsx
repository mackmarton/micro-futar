import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { PackageSizeDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link } from 'react-router-dom';
import { getAllPackageSizes } from '../api/logisticsDeposApi';
import { logisticsNavigationItems } from '../navigation';
import { EntityListShell } from '../shared/EntityListShell';

const valueOrFallback = (value?: number | string) =>
  value === 0 || (typeof value === 'string' && value.length > 0) ? value : 'N/A';

export const LogisticsPackageSizesPage = () => {
  const packageSizesQuery = useQuery({
    queryKey: ['logistics', 'package-sizes'],
    queryFn: getAllPackageSizes,
  });

  const columns = useMemo<DataTableColumn<PackageSizeDTO>[]>(
    () => [
      {
        id: 'name',
        header: 'Név',
        mobileLabel: 'Név',
        cell: (packageSize) => valueOrFallback(packageSize.name),
      },
      {
        id: 'maxLength',
        header: 'Max hossz',
        mobileLabel: 'Max hossz',
        cell: (packageSize) =>
          typeof packageSize.maxLength === 'number' ? `${packageSize.maxLength} cm` : 'N/A',
      },
      {
        id: 'edit',
        header: 'Szerkesztés',
        cell: (packageSize) =>
          typeof packageSize.id === 'number' ? (
            <Link
              to={`/portal/package-sizes/${packageSize.id}/edit`}
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
      title="Csomagméretek"
      activeHref="#/portal/package-sizes"
      navigationItems={logisticsNavigationItems}
      eyebrow="Logisztika"
      heading="Csomagméretek"
      headerActions={
        <Link
          to="/portal/package-sizes/new"
          className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
        >
          Új csomagméret létrehozása
        </Link>
      }
      isLoading={packageSizesQuery.isLoading}
      loadingMessage="A csomagméretek betöltése folyamatban..."
      isError={packageSizesQuery.isError}
      errorMessage="Nem sikerült betölteni a csomagméreteket."
      errorDetail={(packageSizesQuery.error as Error)?.message}
      onRetry={() => {
        void packageSizesQuery.refetch();
      }}
    >
      <DataTable
        data={packageSizesQuery.data ?? []}
        rowKey={(packageSize, index) => `package-size-${packageSize.id ?? packageSize.name ?? index}`}
        title="Csomagméret lista"
        columns={columns}
        emptyMessage="Nincs elérhető csomagméret rekord."
        mobileCardEyebrow="Csomagméret"
      />
    </EntityListShell>
  );
};
