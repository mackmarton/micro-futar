import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { PackageSizeDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link } from 'react-router-dom';
import { getAllPackageSizes } from '../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../navigation';
import { EntityListShell } from '../shared/EntityListShell';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher';

export const LogisticsPackageSizesPage = () => {
  const { t } = useTranslation('packageSize');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const notAvailable = tCommon('status.notAvailable');

  const valueOrFallback = (value?: number | string) =>
    typeof value === 'number' || (typeof value === 'string' && value.length > 0) ? value : notAvailable;

  const packageSizesQuery = useQuery({
    queryKey: ['logistics', 'package-sizes'],
    queryFn: getAllPackageSizes,
  });

  const columns = useMemo<DataTableColumn<PackageSizeDTO>[]>(
    () => [
      {
        id: 'name',
        header: t('list.columns.name'),
        mobileLabel: t('list.columns.name'),
        // packageSize.name backend-ről érkező szabad szöveg, nem fordítjuk.
        cell: (packageSize) => valueOrFallback(packageSize.name),
      },
      {
        id: 'maxLength',
        header: t('list.columns.maxLength'),
        mobileLabel: t('list.columns.maxLength'),
        cell: (packageSize) =>
          typeof packageSize.maxLength === 'number' ? `${packageSize.maxLength} cm` : notAvailable,
      },
      {
        id: 'edit',
        header: tCommon('table.editHeader'),
        cell: (packageSize) =>
          typeof packageSize.id === 'number' ? (
            <Link
              to={`/portal/package-sizes/${packageSize.id}/edit`}
              className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
            >
              {tCommon('buttons.edit')}
            </Link>
          ) : (
            <span className="text-on-surface-variant">{notAvailable}</span>
          ),
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, tCommon],
  );

  return (
    <EntityListShell
      title={t('list.title')}
      activeHref="#/portal/package-sizes"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('list.eyebrow')}
      heading={t('list.heading')}
      headerActions={
        <Link
          to="/portal/package-sizes/new"
          className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
        >
          {t('list.addNew')}
        </Link>
      }
      isLoading={packageSizesQuery.isLoading}
      loadingMessage={t('list.loading')}
      isError={packageSizesQuery.isError}
      errorMessage={t('list.errorHeading')}
      errorDetail={(packageSizesQuery.error as Error)?.message}
      onRetry={() => {
        void packageSizesQuery.refetch();
      }}
    >
      <DataTable
        data={packageSizesQuery.data ?? []}
        rowKey={(packageSize, index) => `package-size-${packageSize.id ?? packageSize.name ?? index}`}
        title={t('list.tableTitle')}
        columns={columns}
        emptyMessage={t('list.empty')}
        mobileCardEyebrow={t('list.mobileEyebrow')}
      />
    </EntityListShell>
  );
};
