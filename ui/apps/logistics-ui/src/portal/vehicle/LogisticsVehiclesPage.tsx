import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { VehicleDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link } from 'react-router-dom';
import { getAllVehicles } from '../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../navigation';
import { EntityListShell } from '../shared/EntityListShell';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher';

const formatNumberWithSpaces = (value: number) => {
  const [integerPart, fractionPart] = value.toString().split('.');
  const formattedIntegerPart = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');

  return fractionPart ? `${formattedIntegerPart}.${fractionPart}` : formattedIntegerPart;
};

export const LogisticsVehiclesPage = () => {
  const { t } = useTranslation('vehicle');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const notAvailable = tCommon('status.notAvailable');

  const valueOrFallback = (value?: number | string) =>
    typeof value === 'number' || (typeof value === 'string' && value.length > 0) ? value : notAvailable;

  const vehiclesQuery = useQuery({
    queryKey: ['logistics', 'vehicles'],
    queryFn: getAllVehicles,
  });

  const columns = useMemo<DataTableColumn<VehicleDTO>[]>(
    () => [
      {
        id: 'registrationNumber',
        header: t('list.columns.registrationNumber'),
        mobileLabel: t('list.columns.registrationNumber'),
        // vehicle.registrationNumber backend-ről érkező szabad szöveg, nem fordítjuk.
        cell: (vehicle) => valueOrFallback(vehicle.registrationNumber),
      },
      {
        id: 'maximumPackableVolume',
        header: t('list.columns.maximumPackableVolume'),
        mobileLabel: t('list.columns.maximumPackableVolume'),
        cell: (vehicle) =>
          typeof vehicle.maximumPackableVolume === 'number'
            ? (
              <>
                {formatNumberWithSpaces(vehicle.maximumPackableVolume)} cm<sup>3</sup>
              </>
            )
            : notAvailable,
      },
      {
        id: 'edit',
        header: tCommon('table.editHeader'),
        cell: (vehicle) =>
          typeof vehicle.id === 'number' ? (
            <Link
              to={`/portal/vehicles/${vehicle.id}/edit`}
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
      title={t('list.title')}
      activeHref="#/portal/vehicles"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('list.eyebrow')}
      heading={t('list.heading')}
      headerActions={
        <Link
          to="/portal/vehicles/new"
          className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
        >
          {t('list.addNew')}
        </Link>
      }
      isLoading={vehiclesQuery.isLoading}
      loadingMessage={t('list.loading')}
      isError={vehiclesQuery.isError}
      errorMessage={t('list.errorHeading')}
      errorDetail={(vehiclesQuery.error as Error)?.message}
      onRetry={() => {
        void vehiclesQuery.refetch();
      }}
    >
      <DataTable
        data={vehiclesQuery.data ?? []}
        rowKey={(vehicle, index) => `vehicle-${vehicle.id ?? vehicle.registrationNumber ?? index}`}
        title={t('list.tableTitle')}
        columns={columns}
        emptyMessage={t('list.empty')}
        mobileCardEyebrow={t('list.mobileEyebrow')}
      />
    </EntityListShell>
  );
};
