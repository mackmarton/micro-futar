import type { PortalLayoutProps } from '@package/shared-ui';

export const logisticsNavigationItems: NonNullable<PortalLayoutProps['navigationItems']> = [
  {
    label: 'Dashboard',
    href: '#/portal/dashboard',
    sideIcon: 'dashboard',
    bottomIcon: 'dashboard',
    onlyLoggedIn: true,
  },
  {
    label: 'Depok',
    href: '#/portal/depos',
    sideIcon: 'warehouse',
    bottomIcon: 'warehouse',
    onlyLoggedIn: true,
  },
  {
    label: 'Futárok',
    href: '#/portal/couriers',
    sideIcon: 'delivery_truck_speed',
    bottomIcon: 'delivery_truck_speed',
    onlyLoggedIn: true,
  },
  {
    label: 'Helyszínek',
    href: '#/portal/locations/regions',
    sideIcon: 'location_city',
    bottomIcon: 'location_city',
    onlyLoggedIn: true,
  },
  {
    label: 'Csomagméretek',
    href: '#/portal/package-sizes',
    sideIcon: 'deployed_code',
    bottomIcon: 'deployed_code',
    onlyLoggedIn: true,
  },
  {
    label: 'Járművek',
    href: '#/portal/vehicles',
    sideIcon: 'airport_shuttle',
    bottomIcon: 'airport_shuttle',
    onlyLoggedIn: true,
  },
];
