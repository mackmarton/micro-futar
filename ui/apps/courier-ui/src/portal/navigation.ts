import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import type { PortalLayoutProps } from '@package/shared-ui';

type CourierNavigationItems = NonNullable<PortalLayoutProps['navigationItems']>;

/**
 * A navigáció címkéi futásidőben fordítandók, ezért ez egy hook, nem egy statikus export -
 * minden fogyasztó (App.tsx és az egyes portál-oldalak, amelyek saját `PortalLayout`-ot
 * rendernek a layout state felülírásához) ezt hívja meg a statikus tömb helyett.
 */
export const useCourierNavigationItems = (): CourierNavigationItems => {
  const { t } = useTranslation('common');

  return useMemo<CourierNavigationItems>(
    () => [
      {
        label: t('nav.pickup'),
        href: '#/portal/shipment-pickup',
        sideIcon: 'warehouse',
        bottomIcon: 'warehouse',
        onlyLoggedIn: true,
      },
      {
        label: t('nav.allocations'),
        href: '#/portal/allocated-packages',
        sideIcon: 'package_2',
        bottomIcon: 'package_2',
        onlyLoggedIn: true,
      },
      {
        label: t('nav.dropoff'),
        href: '#/portal/shipment-dropoff',
        sideIcon: 'local_shipping',
        bottomIcon: 'local_shipping',
        onlyLoggedIn: true,
      },
    ],
    [t],
  );
};
