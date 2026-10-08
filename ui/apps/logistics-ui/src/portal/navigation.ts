import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import type { PortalLayoutProps } from '@package/shared-ui';

/**
 * A nav-item labelek a nyelvváltáshoz futásidőben fordítva kellenek, ezért ez egy hook, nem
 * statikus tömb (lásd `apps/client-ui/src/shared/usePortalNavigation.ts` hasonló mintáját).
 */
export const useLogisticsNavigationItems = (): NonNullable<PortalLayoutProps['navigationItems']> => {
  const { t } = useTranslation('common');

  return useMemo(
    () => [
      {
        label: t('nav.depos'),
        href: '#/portal/depos',
        sideIcon: 'warehouse',
        bottomIcon: 'warehouse',
        onlyLoggedIn: true,
      },
      {
        label: t('nav.couriers'),
        href: '#/portal/couriers',
        sideIcon: 'person',
        bottomIcon: 'person',
        onlyLoggedIn: true,
      },
      {
        label: t('nav.locations'),
        href: '#/portal/locations/regions',
        sideIcon: 'location_city',
        bottomIcon: 'location_city',
        onlyLoggedIn: true,
      },
      {
        label: t('nav.packageSizes'),
        href: '#/portal/package-sizes',
        sideIcon: 'deployed_code',
        bottomIcon: 'deployed_code',
        onlyLoggedIn: true,
      },
      {
        label: t('nav.vehicles'),
        href: '#/portal/vehicles',
        sideIcon: 'delivery_truck_speed',
        bottomIcon: 'delivery_truck_speed',
        onlyLoggedIn: true,
      },
      {
        label: t('nav.currencies'),
        href: '#/portal/currencies',
        sideIcon: 'payments',
        bottomIcon: 'payments',
        onlyLoggedIn: true,
      },
    ],
    [t],
  );
};
