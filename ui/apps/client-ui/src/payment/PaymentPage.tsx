'use client';

import { Suspense, useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import { PortalLayout, cn, useAuth } from '@package/shared-ui';
import type { AppLocale } from '../i18n/createI18nInstance.ts';
import { LanguageSwitcher } from '../i18n/LanguageSwitcher.tsx';
import { usePortalNavigation } from '../shared/usePortalNavigation.ts';
import { getHttpStatus, type PaymentStatus } from './api/paymentsApi.ts';
import { usePaymentStatus } from './hooks/usePaymentStatus.ts';
import { clearPaymentToken, loadPaymentToken } from './paymentTokenStore.ts';
import { redirectToCheckout } from './redirectToCheckout.ts';

export type PaymentPageProps = {
    locale: AppLocale;
};

type Tone = 'neutral' | 'success' | 'error';

const TONE_CLASS: Record<Tone, string> = {
    neutral: 'bg-secondary-container text-on-secondary-container',
    success: 'bg-primary-fixed text-on-primary-fixed-variant',
    error: 'bg-error-container/30 text-error',
};

const STATUS_VIEW: Record<PaymentStatus, { icon: string; tone: Tone }> = {
    PENDING: { icon: 'credit_card', tone: 'neutral' },
    AUTHORIZED: { icon: 'verified', tone: 'success' },
    PAID: { icon: 'paid', tone: 'success' },
    EXPIRED: { icon: 'timer_off', tone: 'error' },
    FAILED: { icon: 'error', tone: 'error' },
    CANCELED: { icon: 'block', tone: 'error' },
    REFUNDED: { icon: 'currency_exchange', tone: 'neutral' },
};

const formatAmount = (amount: number | undefined, currencyCode: string | undefined, locale: AppLocale) => {
    if (amount === undefined) {
        return '-';
    }
    return new Intl.NumberFormat(locale === 'en' ? 'en-US' : 'hu-HU', {
        style: 'currency',
        currency: currencyCode ?? 'HUF',
        maximumFractionDigits: currencyCode === 'HUF' || !currencyCode ? 0 : 2,
    }).format(amount);
};

const parseShipmentId = (value: string | null): number | null => {
    const parsed = Number(value);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
};

/**
 * A `useSearchParams` miatt statikus exportnál csak a böngészőben renderelhető, ezért külön
 * komponens egy `<Suspense>` határon belül.
 */
const PaymentContent = ({ locale }: PaymentPageProps) => {
    const { t } = useTranslation('payment');
    const { routeHref } = usePortalNavigation(locale, 'payment');
    const { user } = useAuth();
    const searchParams = useSearchParams();
    const shipmentId = parseShipmentId(searchParams.get('shipmentId'));
    const outcome = searchParams.get('outcome');

    // A `useSearchParams` miatt ez a komponens csak a böngészőben renderelődik, így a token
    // render közben is kiolvasható.
    const paymentToken = useMemo(
        () => (shipmentId === null ? null : loadPaymentToken(shipmentId)),
        [shipmentId],
    );

    const statusQuery = usePaymentStatus(shipmentId, paymentToken, outcome === 'success');
    const status = statusQuery.data?.status;

    useEffect(() => {
        if (shipmentId !== null && status && status !== 'PENDING') {
            clearPaymentToken(shipmentId);
        }
    }, [shipmentId, status]);

    const [isRedirecting, setIsRedirecting] = useState(false);
    const [checkoutError, setCheckoutError] = useState<string | null>(null);

    const handlePay = async () => {
        if (shipmentId === null) {
            return;
        }
        setIsRedirecting(true);
        setCheckoutError(null);
        try {
            await redirectToCheckout(shipmentId, paymentToken, locale);
        } catch (error) {
            console.error('Failed to start checkout.', error);
            setCheckoutError(t('checkoutError'));
            setIsRedirecting(false);
        }
    };

    if (shipmentId === null) {
        return <Message tone="error" icon="link_off" title={t('invalidLink.title')} text={t('invalidLink.text')} />;
    }

    if (statusQuery.isPending) {
        return <Message tone="neutral" icon="hourglass_top" title={t('loading')} />;
    }

    if (statusQuery.isError) {
        const notFound = getHttpStatus(statusQuery.error) === 404;
        return (
            <Message
                tone="error"
                icon="search_off"
                title={notFound ? t('notFound.title') : t('error.title')}
                text={notFound ? t('notFound.text') : t('error.text')}
            />
        );
    }

    const data = statusQuery.data;
    const view = STATUS_VIEW[data.status ?? 'PENDING'];
    const maxAmount = formatAmount(data.maxAmount, data.currencyCode, locale);
    const capturedAmount = formatAmount(data.capturedAmount, data.currencyCode, locale);
    const isProcessing = data.status === 'PENDING' && outcome === 'success';

    if (isProcessing) {
        return <Message tone="neutral" icon="hourglass_top" title={t('processing.title')} text={t('processing.text')} />;
    }

    return (
        <Message
            tone={view.tone}
            icon={view.icon}
            title={t(`status.${data.status}.title`)}
            text={t(`status.${data.status}.text`, { maxAmount, capturedAmount })}
        >
            {data.status === 'PENDING' ? (
                <>
                    {outcome === 'cancel' ? <p className="text-sm text-on-surface-variant">{t('cancelled')}</p> : null}
                    <button
                        type="button"
                        onClick={() => {
                            void handlePay();
                        }}
                        disabled={isRedirecting}
                        className="inline-flex items-center gap-2 bg-primary text-on-primary px-6 py-3 rounded-lg font-bold hover:bg-on-primary-container transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        <span className="material-symbols-outlined" aria-hidden="true">credit_card</span>
                        {isRedirecting ? t('redirecting') : t('payButton', { maxAmount })}
                    </button>
                    {checkoutError ? <p className="text-sm text-error">{checkoutError}</p> : null}
                </>
            ) : null}

            {data.status === 'EXPIRED' || data.status === 'CANCELED' || data.status === 'FAILED' ? (
                <a
                    href={routeHref('createOrder')}
                    className="inline-flex items-center gap-2 bg-primary text-on-primary px-6 py-3 rounded-lg font-bold hover:bg-on-primary-container transition-all"
                >
                    {t('newOrder')}
                </a>
            ) : null}

            {user && (data.status === 'AUTHORIZED' || data.status === 'PAID') ? (
                <a
                    href={routeHref('dashboard')}
                    className="inline-flex items-center gap-2 text-primary font-bold hover:underline"
                >
                    {t('toDashboard')}
                </a>
            ) : null}
        </Message>
    );
};

type MessageProps = {
    tone: Tone;
    icon: string;
    title: string;
    text?: string;
    children?: React.ReactNode;
};

const Message = ({ tone, icon, title, text, children }: MessageProps) => (
    <section className="bg-surface-container-lowest rounded-2xl border border-outline-variant p-8 md:p-12 text-center flex flex-col items-center gap-4 max-w-2xl mx-auto">
        <div className={cn('w-14 h-14 rounded-full flex items-center justify-center', TONE_CLASS[tone])}>
            <span className="material-symbols-outlined text-3xl" aria-hidden="true">{icon}</span>
        </div>
        <h2 className="font-headline text-2xl font-bold text-on-surface">{title}</h2>
        {text ? <p className="text-on-surface-variant max-w-prose">{text}</p> : null}
        {children}
    </section>
);

export const PaymentPage = ({ locale }: PaymentPageProps) => {
    const { t } = useTranslation(['common', 'payment']);
    const { navigationItems, homeHref, brandSubtitle, routeHref } = usePortalNavigation(locale, 'payment');

    return (
        <PortalLayout
            title={t('payment:title')}
            activeHref={routeHref('createOrder')}
            navigationItems={navigationItems}
            logoHref={homeHref}
            brandSubtitle={brandSubtitle}
            topBarRightSlot={<LanguageSwitcher />}
        >
            <Suspense fallback={null}>
                <PaymentContent locale={locale} />
            </Suspense>
        </PortalLayout>
    );
};
