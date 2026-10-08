'use client';

import {TrackingHero} from './components';
import {TrackingDetailsSection} from './components';
import {useTracking} from './hooks/useTracking.ts';
import { Suspense, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import { PortalLayout, cn } from '@package/shared-ui';
import type { AppLocale } from '../i18n/createI18nInstance.ts';
import { LanguageSwitcher } from '../i18n/LanguageSwitcher.tsx';
import { usePortalNavigation } from '../shared/usePortalNavigation.ts';

export type TrackPackagePageProps = {
    locale: AppLocale;
    className?: string;
};

/**
 * Az URL-ben érkező `trackingNumber` alapján elindítja a keresést. Külön komponensben van,
 * mert a `useSearchParams` miatt statikus exportnál ez a rész csak a böngészőben renderelhető;
 * így a `<Suspense>` határ csak ezt fogja körbe, az oldal többi része előre renderelődik.
 */
const InitialTrackingSearch = ({onSearch}: { onSearch: (trackingNumber: string) => Promise<void> }) => {
    const searchParams = useSearchParams();
    const initialTrackingNumber = searchParams.get('trackingNumber')?.trim() ?? '';

    useEffect(() => {
        if (!initialTrackingNumber) {
            return;
        }

        void onSearch(initialTrackingNumber);
    }, [initialTrackingNumber, onSearch]);

    return null;
};

export const TrackPackagePage = ({locale, className}: TrackPackagePageProps) => {
    const {hasSearchStarted, isLoading, errorMessage, details, search, retry} = useTracking();
    const {t} = useTranslation(['common', 'tracking']);
    const {activeHref, navigationItems, homeHref, brandSubtitle} = usePortalNavigation(locale, 'tracking');

    const handleSearch = (trackingCode: string) => {
        void search(trackingCode);
    };

    return (
        <PortalLayout
            title={t('common:nav.tracking')}
            activeHref={activeHref}
            navigationItems={navigationItems}
            logoHref={homeHref}
            brandSubtitle={brandSubtitle}
            topBarRightSlot={<LanguageSwitcher />}
            contentClassName={cn('px-6 py-8 md:p-12', className)}
        >
                    <Suspense fallback={null}>
                        <InitialTrackingSearch onSearch={search}/>
                    </Suspense>
                    <TrackingHero onSearch={handleSearch}/>

                    {hasSearchStarted && isLoading && (
                        <section className="bg-surface-container-lowest rounded-xl p-6 shadow-sm text-center text-on-surface-variant">
                            {t('tracking:loading')}
                        </section>
                    )}

                    {hasSearchStarted && !isLoading && errorMessage && (
                        <section className="bg-surface-container-lowest rounded-xl p-6 shadow-sm border border-error text-center space-y-4">
                            <p className="text-error">{errorMessage}</p>
                            <button
                                type="button"
                                onClick={() => void retry()}
                                className="px-4 py-2 rounded-lg bg-primary text-on-primary font-semibold hover:bg-on-primary-container transition-all"
                            >
                                {t('tracking:retry')}
                            </button>
                        </section>
                    )}

                    {hasSearchStarted && !isLoading && !errorMessage && details && (
                        <TrackingDetailsSection
                            trackingNumber={details.trackingNumber}
                            statusLabel={details.statusLabel}
                            deliveryTimeValue={details.deliveryTimeValue}
                            progressSteps={details.progressSteps}
                            timelineEvents={details.timelineEvents}
                            shippingAddressPrimary={details.shippingAddressPrimary}
                        />
                    )}
        </PortalLayout>
    );
};

