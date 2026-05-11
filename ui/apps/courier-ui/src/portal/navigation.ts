import type { PortalLayoutProps } from '@package/shared-ui';

export const courierNavigationItems: NonNullable<PortalLayoutProps['navigationItems']> = [
  {
    label: 'Csomag felvétel',
    href: '#/portal/shipment-pickup',
    sideIcon: 'warehouse',
    bottomIcon: 'warehouse',
    onlyLoggedIn: true,
  },
];
