import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { ShipmentManualReviewDTO } from '@package/shared-core/api/LogisticsApiClient';
import { getManualReviews } from '../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../navigation';
import { EntityListShell } from '../shared/EntityListShell';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher';

export const LogisticsManualReviewsPage = () => {
  const { t, i18n } = useTranslation('manualReview');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const notAvailable = tCommon('status.notAvailable');

  const valueOrFallback = (value?: string) => (typeof value === 'string' && value.length > 0 ? value : notAvailable);

  const reviewsQuery = useQuery({
    queryKey: ['logistics', 'manual-reviews'],
    queryFn: getManualReviews,
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
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, tCommon, i18n.language],
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
      headerActions={null}
      isLoading={reviewsQuery.isLoading}
      loadingMessage={t('list.loading')}
      isError={reviewsQuery.isError}
      errorMessage={t('list.errorHeading')}
      errorDetail={(reviewsQuery.error as Error)?.message}
      onRetry={() => {
        void reviewsQuery.refetch();
      }}
    >
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
