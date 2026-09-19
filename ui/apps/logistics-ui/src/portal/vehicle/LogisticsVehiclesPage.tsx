import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { VehicleDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link } from 'react-router-dom';
import { getAllVehicles } from '../api/logisticsDeposApi';
import { logisticsNavigationItems } from '../navigation';
import { EntityListShell } from '../shared/EntityListShell';

const valueOrFallback = (value?: number | string) =>
  value === 0 || (typeof value === 'string' && value.length > 0) ? value : 'N/A';

const formatNumberWithSpaces = (value: number) => {
  const [integerPart, fractionPart] = value.toString().split('.');
  const formattedIntegerPart = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');

  return fractionPart ? `${formattedIntegerPart}.${fractionPart}` : formattedIntegerPart;
};

export const LogisticsVehiclesPage = () => {
  const vehiclesQuery = useQuery({
    queryKey: ['logistics', 'vehicles'],
    queryFn: getAllVehicles,
  });

  const columns = useMemo<DataTableColumn<VehicleDTO>[]>(
    () => [
      {
        id: 'registrationNumber',
        header: 'Rendszám',
        mobileLabel: 'Rendszám',
        cell: (vehicle) => valueOrFallback(vehicle.registrationNumber),
      },
      {
        id: 'maximumPackableVolume',
        header: 'Max térfogat',
        mobileLabel: 'Max térfogat',
        cell: (vehicle) =>
          typeof vehicle.maximumPackableVolume === 'number'
            ? (
              <>
                {formatNumberWithSpaces(vehicle.maximumPackableVolume)} cm<sup>3</sup>
              </>
            )
            : 'N/A',
      },
      {
        id: 'edit',
        header: 'Szerkesztés',
        cell: (vehicle) =>
          typeof vehicle.id === 'number' ? (
            <Link
              to={`/portal/vehicles/${vehicle.id}/edit`}
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
      title="Járművek"
      activeHref="#/portal/vehicles"
      navigationItems={logisticsNavigationItems}
      eyebrow="Logisztika"
      heading="Járművek"
      headerActions={
        <Link
          to="/portal/vehicles/new"
          className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
        >
          Új jármű létrehozása
        </Link>
      }
      isLoading={vehiclesQuery.isLoading}
      loadingMessage="A járművek betöltése folyamatban..."
      isError={vehiclesQuery.isError}
      errorMessage="Nem sikerült betölteni a járműveket."
      errorDetail={(vehiclesQuery.error as Error)?.message}
      onRetry={() => {
        void vehiclesQuery.refetch();
      }}
    >
      <DataTable
        data={vehiclesQuery.data ?? []}
        rowKey={(vehicle, index) => `vehicle-${vehicle.id ?? vehicle.registrationNumber ?? index}`}
        title="Jármű lista"
        columns={columns}
        emptyMessage="Nincs elérhető jármű rekord."
        mobileCardEyebrow="Jármű"
      />
    </EntityListShell>
  );
};
