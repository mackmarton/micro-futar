import type { PortalLayoutProps } from '@package/shared-ui';

export const courierNavigationItems: NonNullable<PortalLayoutProps['navigationItems']> = [
  {
    label: 'Csomag felvétel',
    href: '#/portal/shipment-pickup',
    sideIcon: 'warehouse',
    bottomIcon: 'warehouse',
    onlyLoggedIn: true,
  },
  {
    label: 'Kiosztott csomagok',
    href: '#/portal/allocated-packages',
    sideIcon: 'package_2',
    bottomIcon: 'package_2',
    onlyLoggedIn: true,
  },
  {
    label: 'Csomag leadás',
    href: '#/portal/shipment-dropoff',
    sideIcon: 'local_shipping',
    bottomIcon: 'local_shipping',
    onlyLoggedIn: true,
  },
];
