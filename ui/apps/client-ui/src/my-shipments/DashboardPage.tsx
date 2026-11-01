'use client';

import { useCallback, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { PortalLayout, useAuth } from '@package/shared-ui';
import {ShipmentStats} from './components/ShipmentStats.tsx';
import {ShipmentTable} from "./components/ShipmentTable.tsx";
import {useUserShipments} from './hooks/useUserShipments.ts';
import type { AppLocale } from '../i18n/createI18nInstance.ts';
import { LanguageSwitcher } from '../i18n/LanguageSwitcher.tsx';
import { usePortalNavigation } from '../shared/usePortalNavigation.ts';

export type DashboardPageProps = {
    locale: AppLocale;
};

export const DashboardPage = ({locale}: DashboardPageProps) => {
    const {t} = useTranslation(['common', 'dashboard']);
    const {activeHref, navigationItems, routeHref, homeHref, brandSubtitle} = usePortalNavigation(locale, 'dashboard');
    const paymentHref = routeHref('payment');
    const paymentHrefFor = useCallback((shipmentId: number) => `${paymentHref}?shipmentId=${shipmentId}`, [paymentHref]);
    const {user, isLoading: isAuthLoading, login} = useAuth();
    const isLoggedIn = !isAuthLoading && user !== null;
    const {shipments, stats, isLoading, errorMessage, retry} = useUserShipments(paymentHrefFor, isLoggedIn);

    // A saját csomagok csak bejelentkezve érhetők el: vendéget a bejelentkezésre küldjük, onnan
    // ide tér vissza.
    useEffect(() => {
        if (!isAuthLoading && !user) {
            login();
        }
    }, [isAuthLoading, user, login]);

    return (
        <PortalLayout
            title={t('common:nav.dashboard')}
            activeHref={activeHref}
            navigationItems={navigationItems}
            logoHref={homeHref}
            brandSubtitle={brandSubtitle}
            topBarRightSlot={<LanguageSwitcher />}
            contentClassName="flex-grow"
        >
            {!isLoggedIn ? (
                <section className="bg-surface-container-lowest rounded-2xl border border-outline-variant p-8 text-center text-on-surface-variant">
                    {t('dashboard:redirectingToLogin')}
                </section>
            ) : (
                <>
                    <ShipmentStats stats={stats}/>

                    <div className="flex justify-between items-end mb-8 gap-6">
                        <div>
                            <h3 className="font-headline text-2xl font-bold text-on-surface">{t('dashboard:heading')}</h3>
                            <p className="text-on-surface-variant">
                                {t('dashboard:subtitle')}
                            </p>
                        </div>

                        <a
                            href={routeHref('createOrder')}
                            className="hidden md:flex bg-primary text-on-primary px-6 py-3 rounded-lg font-bold items-center gap-2 hover:bg-on-primary-container transition-all"
                        >
              <span className="material-symbols-outlined" aria-hidden="true">
                local_shipping
              </span>
                            {t('dashboard:newOrderButton')}
                        </a>
                    </div>

                    {isLoading ? (
                        <section className="bg-surface-container-lowest rounded-2xl border border-outline-variant p-8 text-center text-on-surface-variant">
                            {t('dashboard:loading')}
                        </section>
                    ) : null}

                    {!isLoading && errorMessage ? (
                        <section className="bg-error-container/30 rounded-2xl border border-error/30 p-8 text-center">
                            <p className="text-error font-medium mb-4">{errorMessage}</p>
                            <button
                                type="button"
                                onClick={() => {
                                    void retry();
                                }}
                                className="inline-flex items-center gap-2 bg-primary text-on-primary px-4 py-2 rounded-lg font-medium hover:bg-on-primary-container transition-all"
                            >
                                <span className="material-symbols-outlined" aria-hidden="true">
                                    refresh
                                </span>
                                {t('dashboard:error.retry')}
                            </button>
                        </section>
                    ) : null}

                    {!isLoading && !errorMessage && shipments.length === 0 ? (
                        <section className="bg-surface-container-lowest rounded-2xl border border-outline-variant p-8 text-center text-on-surface-variant">
                            {t('dashboard:empty')}
                        </section>
                    ) : null}

                    {!isLoading && !errorMessage && shipments.length > 0 ? (
                        <ShipmentTable shipments={shipments}/>
                    ) : null}
                </>
            )}
        </PortalLayout>
    );
};

