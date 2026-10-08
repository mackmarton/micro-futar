import i18next from 'i18next';
import { initReactI18next } from 'react-i18next';
import huCommon from './locales/hu/common.json';
import huDepo from './locales/hu/depo.json';
import huCourier from './locales/hu/courier.json';
import huLocation from './locales/hu/location.json';
import huPackageSize from './locales/hu/packageSize.json';
import huVehicle from './locales/hu/vehicle.json';
import huCurrency from './locales/hu/currency.json';
import huLanding from './locales/hu/landing.json';
import enCommon from './locales/en/common.json';
import enDepo from './locales/en/depo.json';
import enCourier from './locales/en/courier.json';
import enLocation from './locales/en/location.json';
import enPackageSize from './locales/en/packageSize.json';
import enVehicle from './locales/en/vehicle.json';
import enCurrency from './locales/en/currency.json';
import enLanding from './locales/en/landing.json';

export type AppLocale = 'hu' | 'en';

export const SUPPORTED_LOCALES: AppLocale[] = ['hu', 'en'];
export const DEFAULT_LOCALE: AppLocale = 'hu';
export const LOCALE_STORAGE_KEY = 'micro-futar-locale';

const resources = {
  hu: {
    common: huCommon,
    depo: huDepo,
    courier: huCourier,
    location: huLocation,
    packageSize: huPackageSize,
    vehicle: huVehicle,
    currency: huCurrency,
    landing: huLanding,
  },
  en: {
    common: enCommon,
    depo: enDepo,
    courier: enCourier,
    location: enLocation,
    packageSize: enPackageSize,
    vehicle: enVehicle,
    currency: enCurrency,
    landing: enLanding,
  },
};

const isSupportedLocale = (value: string | null): value is AppLocale =>
  value === 'hu' || value === 'en';

const readStoredLocale = (): AppLocale => {
  try {
    const stored = window.localStorage.getItem(LOCALE_STORAGE_KEY);
    return isSupportedLocale(stored) ? stored : DEFAULT_LOCALE;
  } catch {
    return DEFAULT_LOCALE;
  }
};

/**
 * Egyetlen globális i18next instance, mert a logistics-ui sima Vite SPA (nincs statikus export,
 * nincs SEO-követelmény) – a nyelv csak runtime állapot, a `localStorage`-ból olvasva induláskor,
 * nem kell per-locale instance-okat létrehozni útvonal-prefixekhez, mint a client-ui-ban.
 */
void i18next.use(initReactI18next).init({
  lng: readStoredLocale(),
  fallbackLng: DEFAULT_LOCALE,
  defaultNS: 'common',
  ns: ['common', 'depo', 'courier', 'location', 'packageSize', 'vehicle', 'currency', 'landing'],
  resources,
  interpolation: { escapeValue: false },
  react: { useSuspense: false },
});

export default i18next;
