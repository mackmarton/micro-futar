import i18next, { type i18n as I18nInstance } from 'i18next';
import huCommon from './locales/hu/common.json';
import huTracking from './locales/hu/tracking.json';
import huDashboard from './locales/hu/dashboard.json';
import huCreateOrder from './locales/hu/createOrder.json';
import huLanding from './locales/hu/landing.json';
import enCommon from './locales/en/common.json';
import enTracking from './locales/en/tracking.json';
import enDashboard from './locales/en/dashboard.json';
import enCreateOrder from './locales/en/createOrder.json';
import enLanding from './locales/en/landing.json';

export type AppLocale = 'hu' | 'en';

export const SUPPORTED_LOCALES: AppLocale[] = ['hu', 'en'];
export const DEFAULT_LOCALE: AppLocale = 'hu';

const resources = {
  hu: { common: huCommon, tracking: huTracking, dashboard: huDashboard, createOrder: huCreateOrder, landing: huLanding },
  en: { common: enCommon, tracking: enTracking, dashboard: enDashboard, createOrder: enCreateOrder, landing: enLanding },
};

/**
 * Minden lokalizált gyökér-layout (app/(hu), app/en) saját instance-ot hoz létre, hogy a
 * statikus exportnál a build-időben renderelt HTML a megfelelő nyelven készüljön el, ne egy
 * globális, runtime-ban váltogatott singleton állapotától függjön.
 */
export const createI18nInstance = (locale: AppLocale): I18nInstance => {
  const instance = i18next.createInstance();

  void instance.init({
    lng: locale,
    fallbackLng: DEFAULT_LOCALE,
    defaultNS: 'common',
    ns: ['common', 'tracking', 'dashboard', 'createOrder', 'landing'],
    resources,
    interpolation: { escapeValue: false },
    react: { useSuspense: false },
  });

  return instance;
};
