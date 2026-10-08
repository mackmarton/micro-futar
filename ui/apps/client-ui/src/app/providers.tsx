'use client';

import { useState, type ReactNode } from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '@package/shared-ui';
import { createAppQueryClient } from '@package/shared-core';
import { LocaleProvider } from '../i18n/LocaleProvider.tsx';
import type { AppLocale } from '../i18n/createI18nInstance.ts';

export const Providers = ({ locale, children }: { locale: AppLocale; children: ReactNode }) => {
  const [queryClient] = useState(createAppQueryClient);

  return (
    <LocaleProvider locale={locale}>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>{children}</AuthProvider>
      </QueryClientProvider>
    </LocaleProvider>
  );
};
