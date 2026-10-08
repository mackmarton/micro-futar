import { useMutation } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { PortalLayout } from '@package/shared-ui';
import { useCourierNavigationItems } from '../navigation.ts';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher.tsx';
import { ManifestDataTable } from '../pickup/components/ManifestDataTable.tsx';
import { fulfillAllPickupsForCurrentDay } from './api/courierDropoffApi.ts';
import { useCourierDropoffs } from './hooks/useCourierDropoffs.ts';

const toDropoffAllErrorMessage = (error: unknown, fallbackMessage: string): string => {
  if (typeof error === 'object' && error !== null && 'error' in error) {
    const responseError = (error as { error?: { message?: string } }).error?.message;
    if (responseError) {
      return responseError;
    }
  }

  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message;
  }

  return fallbackMessage;
};

export const CourierDropoffPage = () => {
  const { t } = useTranslation(['dropoff', 'common']);
  const courierNavigationItems = useCourierNavigationItems();
  const { assignments, waitingDropoffCount, isLoading, errorMessage, retry } = useCourierDropoffs();

  const dropoffAllMutation = useMutation({
    mutationFn: fulfillAllPickupsForCurrentDay,
    onSuccess: () => {
      void retry();
    },
  });
  const dropoffAllErrorMessage = dropoffAllMutation.isError
    ? toDropoffAllErrorMessage(dropoffAllMutation.error, t('dropoff:dropoffAllError'))
    : null;

  return (
    <PortalLayout
      title={t('common:nav.dropoff')}
      activeHref="#/portal/shipment-dropoff"
      navigationItems={courierNavigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      brandSubtitle={t('common:brand.subtitle')}
    >
      <div className="space-y-6 md:space-y-8">
        <section className="rounded-xl bg-surface-container-low p-6 md:p-8">
          <div className="mt-4 flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
            <div>
              <h1 className="text-4xl font-headline font-bold leading-tight text-on-surface md:text-5xl">
                {t('dropoff:heading.prefix')} <span className="text-on-primary-container">{t('dropoff:heading.highlight')}</span>
              </h1>
              <p className="mt-3 font-body text-on-surface-variant">
                {isLoading ? t('dropoff:subtitle.loading') : t('dropoff:subtitle.waiting', { count: waitingDropoffCount })}
              </p>
            </div>

            <div className="flex flex-col gap-3 rounded-full bg-surface-container-lowest px-4 py-3 shadow-[0_24px_42px_rgba(11,28,48,0.05)] sm:flex-row sm:items-center sm:justify-between sm:px-5">
              <div className="px-2">
                <p className="mt-1 font-body text-sm text-on-surface">
                  {isLoading ? t('common:status.loading') : t('dropoff:summary.detected', { count: waitingDropoffCount })}
                </p>
              </div>
              <button
                type="button"
                onClick={() => {
                  dropoffAllMutation.mutate();
                }}
                disabled={dropoffAllMutation.isPending || waitingDropoffCount === 0}
                className="inline-flex items-center justify-center gap-2 rounded-lg bg-primary px-4 py-2.5 font-body font-medium text-on-primary transition-all duration-200 enabled:hover:bg-[linear-gradient(95deg,#000000_0%,#0c9488_100%)] disabled:bg-gray-700"
              >
                <span className="material-symbols-outlined text-base leading-none" aria-hidden="true">
                  done_all
                </span>
                {dropoffAllMutation.isPending ? t('common:status.pending') : t('dropoff:dropoffAllButton.label')}
              </button>
            </div>
          </div>
          {dropoffAllErrorMessage ? (
            <p className="mt-4 font-body text-sm text-red-600">{dropoffAllErrorMessage}</p>
          ) : null}
        </section>

        {errorMessage ? (
          <section className="rounded-xl bg-surface-container-low p-6 md:p-7">
            <p className="font-body text-sm text-red-600">{errorMessage}</p>
            <button
              type="button"
              onClick={() => {
                void retry();
              }}
              className="mt-4 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-medium text-on-primary"
            >
              {t('common:actions.retry')}
            </button>
          </section>
        ) : null}

        <section className="space-y-4">
          <div className="rounded-xl bg-surface-container-low p-6 md:p-7">
            <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('dropoff:section.eyebrow')}</p>
            <h2 className="mt-2 text-2xl font-headline font-bold text-on-surface">{t('dropoff:section.heading')}</h2>
          </div>

          <ManifestDataTable assignments={assignments} />
        </section>
      </div>
    </PortalLayout>
  );
};
