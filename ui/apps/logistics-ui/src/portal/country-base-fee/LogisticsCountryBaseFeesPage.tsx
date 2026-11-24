import { useMemo } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { CountryBaseFeeDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link } from 'react-router-dom';
import {
  deleteCountryBaseFee,
  getAllCountries,
  getAllCountryBaseFees,
  getAllPackageSizes,
} from '../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../navigation';
import { EntityListShell } from '../shared/EntityListShell';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher';

export const LogisticsCountryBaseFeesPage = () => {
  const { t } = useTranslation('countryBaseFee');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const queryClient = useQueryClient();
  const notAvailable = tCommon('status.notAvailable');

  const feesQuery = useQuery({
    queryKey: ['logistics', 'country-base-fees'],
    queryFn: getAllCountryBaseFees,
  });
  const countriesQuery = useQuery({ queryKey: ['logistics', 'countries'], queryFn: getAllCountries });
  const packageSizesQuery = useQuery({ queryKey: ['logistics', 'package-sizes'], queryFn: getAllPackageSizes });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteCountryBaseFee(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['logistics', 'country-base-fees'] }),
  });

  const countryNames = useMemo(
    () => new Map((countriesQuery.data ?? []).map((country) => [country.id, country.name])),
    [countriesQuery.data],
  );
  const packageSizeNames = useMemo(
    () => new Map((packageSizesQuery.data ?? []).map((packageSize) => [packageSize.id, packageSize.name])),
    [packageSizesQuery.data],
  );

  const columns = useMemo<DataTableColumn<CountryBaseFeeDTO>[]>(
    () => [
      {
        id: 'country',
        header: t('list.columns.country'),
        mobileLabel: t('list.columns.country'),
        // A név backend-ről érkező szabad szöveg, nem fordítjuk.
        cell: (fee) => countryNames.get(fee.countryId) ?? fee.countryId ?? notAvailable,
      },
      {
        id: 'packageSize',
        header: t('list.columns.packageSize'),
        mobileLabel: t('list.columns.packageSize'),
        cell: (fee) => packageSizeNames.get(fee.packageSizeId) ?? fee.packageSizeId ?? notAvailable,
      },
      {
        id: 'price',
        header: t('list.columns.price'),
        mobileLabel: t('list.columns.price'),
        cell: (fee) => (typeof fee.price === 'number' ? fee.price : notAvailable),
      },
      {
        id: 'actions',
        header: tCommon('table.editHeader'),
        cell: (fee) =>
          typeof fee.id === 'number' ? (
            <div className="flex flex-wrap gap-2">
              <Link
                to={`/portal/country-base-fees/${fee.id}/edit`}
                className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
              >
                {tCommon('buttons.edit')}
              </Link>
              <button
                type="button"
                disabled={deleteMutation.isPending}
                onClick={() => {
                  if (window.confirm(t('list.deleteConfirm'))) {
                    deleteMutation.mutate(fee.id as number);
                  }
                }}
                className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container disabled:cursor-not-allowed disabled:opacity-70"
              >
                {t('list.delete')}
              </button>
            </div>
          ) : (
            <span className="text-on-surface-variant">{notAvailable}</span>
          ),
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, tCommon, countryNames, packageSizeNames, deleteMutation.isPending],
  );

  return (
    <EntityListShell
      title={t('list.title')}
      activeHref="#/portal/country-base-fees"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('list.eyebrow')}
      heading={t('list.heading')}
      headerActions={
        <Link
          to="/portal/country-base-fees/new"
          className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
        >
          {t('list.addNew')}
        </Link>
      }
      isLoading={feesQuery.isLoading}
      loadingMessage={t('list.loading')}
      isError={feesQuery.isError}
      errorMessage={t('list.errorHeading')}
      errorDetail={(feesQuery.error as Error)?.message}
      onRetry={() => {
        void feesQuery.refetch();
      }}
    >
      <p className="mb-4 font-body text-on-surface-variant">{t('list.description')}</p>
      {deleteMutation.isError ? (
        <div className="mb-4 rounded-xl bg-surface-container-lowest p-4">
          <p className="font-body text-on-surface">
            {(deleteMutation.error as Error)?.message ?? t('list.deleteFailed')}
          </p>
        </div>
      ) : null}
      <DataTable
        data={feesQuery.data ?? []}
        rowKey={(fee, index) => `country-base-fee-${fee.id ?? index}`}
        title={t('list.tableTitle')}
        columns={columns}
        emptyMessage={t('list.empty')}
        mobileCardEyebrow={t('list.mobileEyebrow')}
      />
    </EntityListShell>
  );
};
