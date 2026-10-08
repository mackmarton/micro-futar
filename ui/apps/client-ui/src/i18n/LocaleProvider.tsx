'use client';

import { useState, type ReactNode } from 'react';
import { I18nextProvider } from 'react-i18next';
import { createI18nInstance, type AppLocale } from './createI18nInstance';

export const LocaleProvider = ({ locale, children }: { locale: AppLocale; children: ReactNode }) => {
  const [i18n] = useState(() => createI18nInstance(locale));

  return <I18nextProvider i18n={i18n}>{children}</I18nextProvider>;
};
