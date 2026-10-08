import i18next from 'i18next';
import { initReactI18next } from 'react-i18next';
import huCommon from './locales/hu/common.json';
import huLanding from './locales/hu/landing.json';
import huManifest from './locales/hu/manifest.json';
import huPickup from './locales/hu/pickup.json';
import huDropoff from './locales/hu/dropoff.json';
import huAllocations from './locales/hu/allocations.json';
import enCommon from './locales/en/common.json';
import enLanding from './locales/en/landing.json';
import enManifest from './locales/en/manifest.json';
import enPickup from './locales/en/pickup.json';
import enDropoff from './locales/en/dropoff.json';
import enAllocations from './locales/en/allocations.json';

export type AppLocale = 'hu' | 'en';

export const SUPPORTED_LOCALES: AppLocale[] = ['hu', 'en'];
export const DEFAULT_LOCALE: AppLocale = 'hu';
export const LOCALE_STORAGE_KEY = 'micro-futar-locale';

const resources = {
  hu: {
    common: huCommon,
    landing: huLanding,
    manifest: huManifest,
    pickup: huPickup,
    dropoff: huDropoff,
    allocations: huAllocations,
  },
  en: {
    common: enCommon,
    landing: enLanding,
    manifest: enManifest,
    pickup: enPickup,
    dropoff: enDropoff,
    allocations: enAllocations,
  },
};

const isSupportedLocale = (value: string | null): value is AppLocale =>
  value !== null && (SUPPORTED_LOCALES as string[]).includes(value);

const readStoredLocale = (): AppLocale => {
  try {
    const storedLocale = localStorage.getItem(LOCALE_STORAGE_KEY);
    return isSupportedLocale(storedLocale) ? storedLocale : DEFAULT_LOCALE;
  } catch {
    return DEFAULT_LOCALE;
  }
};

// Ez a courier-ui egyetlen, globális i18next instance-a: ellentétben a client-ui statikusan
// exportált, nyelvenként külön gyökér-layoutjával, ez egy sima Vite SPA - a nyelv nem URL-rész,
// hanem runtime-ban váltogatott állapot, ami localStorage-ban perzisztálódik.
void i18next.use(initReactI18next).init({
  lng: readStoredLocale(),
  fallbackLng: DEFAULT_LOCALE,
  defaultNS: 'common',
  ns: ['common', 'landing', 'manifest', 'pickup', 'dropoff', 'allocations'],
  resources,
  interpolation: { escapeValue: false },
  react: { useSuspense: false },
});

i18next.on('languageChanged', (locale) => {
  try {
    localStorage.setItem(LOCALE_STORAGE_KEY, locale);
  } catch {
    // localStorage elérhetetlen (pl. privát böngészés) - a nyelv ettől futásidőben még működik.
  }

  // A `<html lang>`-et a login redirect (AuthContext.login a shared-ui-ban) olvassa ki, hogy a
  // Keycloak login oldal is a jelenlegi UI-nyelven jelenjen meg.
  document.documentElement.lang = locale;
});

// Kezdeti beállítás induláskor is (a `languageChanged` esemény csak nyelvváltáskor tüzel).
document.documentElement.lang = i18next.language;

export default i18next;
