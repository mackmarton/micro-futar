'use client';

import { cn } from '@package/shared-ui/cn';
import { HeroSection } from './components/HeroSection.tsx';
import { LandingNavBar } from './components/LandingNavBar.tsx';
import { LegacyHashRedirect } from './components/LegacyHashRedirect.tsx';
import { ProcessSection } from './components/ProcessSection.tsx';
import type { AppLocale } from '../i18n/createI18nInstance.ts';

export type LandingPageProps = {
  locale: AppLocale;
  className?: string;
};

export const LandingPage = ({ locale, className }: LandingPageProps) => {
  return (
    <div className={cn('bg-surface text-on-surface min-h-screen selection:bg-primary-fixed selection:text-on-primary-fixed', className)}>
      <LegacyHashRedirect />
      <LandingNavBar locale={locale} />

      <main className="pt-24">
        <HeroSection locale={locale} />
        <ProcessSection />
      </main>

    </div>
  );
};

