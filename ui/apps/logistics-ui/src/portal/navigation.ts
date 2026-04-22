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
];

