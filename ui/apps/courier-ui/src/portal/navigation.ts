import type { PortalLayoutProps } from '@package/shared-ui';

export const courierNavigationItems: NonNullable<PortalLayoutProps['navigationItems']> = [
  {
    label: 'Főoldal',
    href: '#/portal/dashboard',
    sideIcon: 'home',
    bottomIcon: 'home',
    onlyLoggedIn: true,
  },
];
