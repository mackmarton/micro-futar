import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import type { AppLocale } from '../i18n/createI18nInstance.ts';

const basePathFor = (locale: AppLocale) => (locale === 'en' ? '/en' : '');

export type PortalRoute = 'dashboard' | 'createOrder' | 'tracking';

const ROUTE_PATH: Record<PortalRoute, string> = {
  dashboard: '/portal/dashboard',
  createOrder: '/portal/create-order',
  tracking: '/portal/tracking',
};

/**
 * A `PortalLayout` (shared-ui) alapértelmezett nav-itemjei magyar szöveget és prefix nélküli
 * hrefet adnának az `/en` nézetben is, ezért minden portál-oldal saját, lokalizált listát ad át.
 */
export const usePortalNavigation = (locale: AppLocale, activeRoute: PortalRoute) => {
  const { t } = useTranslation('common');
  const basePath = basePathFor(locale);

  const navigationItems = useMemo(
    () => [
      {
        label: t('nav.dashboard'),
        href: `${basePath}${ROUTE_PATH.dashboard}`,
        sideIcon: 'package_2',
        bottomIcon: 'home',
        onlyLoggedIn: true,
      },
      {
        label: t('nav.createOrder'),
        href: `${basePath}${ROUTE_PATH.createOrder}`,
        sideIcon: 'add_circle',
        bottomIcon: 'add_box',
      },
      {
        label: t('nav.tracking'),
        href: `${basePath}${ROUTE_PATH.tracking}`,
        sideIcon: 'local_shipping',
        bottomIcon: 'local_shipping',
      },
    ],
    [basePath, t],
  );

  return {
    basePath,
    homeHref: basePath || '/',
    brandSubtitle: t('brandSubtitle'),
    activeHref: `${basePath}${ROUTE_PATH[activeRoute]}`,
    routeHref: (route: PortalRoute) => `${basePath}${ROUTE_PATH[route]}`,
    navigationItems,
  };
};
