import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { ShipmentManualReviewDTO } from '@package/shared-core/api/LogisticsApiClient';
import { getManualReviews, resolveManualReview } from '../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../navigation';
import { EntityListShell } from '../shared/EntityListShell';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher';

export const LogisticsManualReviewsPage = () => {
  const { t, i18n } = useTranslation('manualReview');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const notAvailable = tCommon('status.notAvailable');

  const valueOrFallback = (value?: string) => (typeof value === 'string' && value.length > 0 ? value : notAvailable);

  const queryClient = useQueryClient();
  const [includeResolved, setIncludeResolved] = useState(false);

  const reviewsQuery = useQuery({
    queryKey: ['logistics', 'manual-reviews', includeResolved],
    queryFn: () => getManualReviews(includeResolved),
  });

  const resolveMutation = useMutation({
    mutationFn: resolveManualReview,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['logistics', 'manual-reviews'] }),
  });

  const columns = useMemo<DataTableColumn<ShipmentManualReviewDTO>[]>(
    () => [
      {
        id: 'parcelNumber',
        header: t('list.columns.parcelNumber'),
        mobileLabel: t('list.columns.parcelNumber'),
        cell: (review) => valueOrFallback(review.parcelNumber),
      },
      {
        id: 'recipientAddress',
        header: t('list.columns.recipientAddress'),
        mobileLabel: t('list.columns.recipientAddress'),
        cell: (review) => valueOrFallback(review.recipientAddress),
      },
      {
        id: 'status',
        header: t('list.columns.status'),
        mobileLabel: t('list.columns.status'),
        cell: (review) => (review.delivered ? t('list.statusDelivered') : t('list.statusPickedUp')),
      },
      {
        id: 'reason',
        header: t('list.columns.reason'),
        mobileLabel: t('list.columns.reason'),
        // review.reason backend-ről érkező szabad szöveg, nem fordítjuk.
        cell: (review) => valueOrFallback(review.reason),
      },
      {
        id: 'flaggedAt',
        header: t('list.columns.flaggedAt'),
        mobileLabel: t('list.columns.flaggedAt'),
        cell: (review) => (review.flaggedAt ? new Date(review.flaggedAt).toLocaleString(i18n.language) : notAvailable),
      },
      {
        id: 'resolve',
        header: t('list.columns.resolve'),
        cell: (review) =>
          review.resolvedAt ? (
            <span className="text-on-surface-variant">
              {t('list.resolvedBy', {
                by: review.resolvedBy ?? notAvailable,
                at: new Date(review.resolvedAt).toLocaleString(i18n.language),
              })}
            </span>
          ) : (
            <button
              type="button"
              disabled={typeof review.shipmentId !== 'number' || resolveMutation.isPending}
              onClick={() => {
                if (typeof review.shipmentId === 'number') {
                  resolveMutation.mutate(review.shipmentId);
                }
              }}
              className="inline-flex items-center rounded-lg bg-primary px-3 py-1.5 text-sm font-semibold text-on-primary transition-colors hover:bg-on-primary-container disabled:opacity-50"
            >
              {t('list.resolveButton')}
            </button>
          ),
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, tCommon, i18n.language, resolveMutation.isPending],
  );

  return (
    <EntityListShell
      title={t('list.title')}
      activeHref="#/portal/manual-reviews"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('list.eyebrow')}
      heading={t('list.heading')}
      contextInfo={<p className="mt-2 font-body text-on-surface-variant">{t('list.description')}</p>}
      headerActions={
        <label className="inline-flex items-center gap-2 font-body text-on-surface">
          <input
            type="checkbox"
            checked={includeResolved}
            onChange={(event) => setIncludeResolved(event.target.checked)}
          />
          {t('list.showResolved')}
        </label>
      }
      isLoading={reviewsQuery.isLoading}
      loadingMessage={t('list.loading')}
      isError={reviewsQuery.isError}
      errorMessage={t('list.errorHeading')}
      errorDetail={(reviewsQuery.error as Error)?.message}
      onRetry={() => {
        void reviewsQuery.refetch();
      }}
    >
      {resolveMutation.isError ? (
        <p className="mb-3 font-body text-error">{t('list.resolveFailed')}</p>
      ) : null}
      <DataTable
        data={reviewsQuery.data ?? []}
        rowKey={(review, index) => `manual-review-${review.shipmentId ?? index}`}
        title={t('list.tableTitle')}
        columns={columns}
        emptyMessage={t('list.empty')}
        mobileCardEyebrow={t('list.mobileEyebrow')}
      />
    </EntityListShell>
  );
};
