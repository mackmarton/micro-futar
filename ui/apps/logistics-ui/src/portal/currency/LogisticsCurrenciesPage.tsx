import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { CurrencyDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link } from 'react-router-dom';
import { getAllCurrencies } from '../api/logisticsDeposApi';
import { useLogisticsNavigationItems } from '../navigation';
import { EntityListShell } from '../shared/EntityListShell';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher';

export const LogisticsCurrenciesPage = () => {
  const { t } = useTranslation('currency');
  const { t: tCommon } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();
  const notAvailable = tCommon('status.notAvailable');

  const valueOrFallback = (value?: string) => (typeof value === 'string' && value.length > 0 ? value : notAvailable);

  const currenciesQuery = useQuery({
    queryKey: ['logistics', 'currencies'],
    queryFn: getAllCurrencies,
  });

  const columns = useMemo<DataTableColumn<CurrencyDTO>[]>(
    () => [
      {
        id: 'code',
        header: t('list.columns.code'),
        mobileLabel: t('list.columns.code'),
        // currency.code backend-ről érkező adat, nem fordítjuk.
        cell: (currency) => valueOrFallback(currency.code),
      },
      {
        id: 'name',
        header: t('list.columns.name'),
        mobileLabel: t('list.columns.name'),
        // currency.name backend-ről érkező szabad szöveg, nem fordítjuk.
        cell: (currency) => valueOrFallback(currency.name),
      },
      {
        id: 'symbol',
        header: t('list.columns.symbol'),
        mobileLabel: t('list.columns.symbol'),
        // currency.symbol backend-ről érkező adat, nem fordítjuk.
        cell: (currency) => valueOrFallback(currency.symbol),
      },
      {
        id: 'edit',
        header: tCommon('table.editHeader'),
        cell: (currency) =>
          typeof currency.id === 'number' ? (
            <Link
              to={`/portal/currencies/${currency.id}/edit`}
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
      activeHref="#/portal/currencies"
      navigationItems={navigationItems}
      topBarRightSlot={<LanguageSwitcher />}
      eyebrow={t('list.eyebrow')}
      heading={t('list.heading')}
      headerActions={
        <Link
          to="/portal/currencies/new"
          className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
        >
          {t('list.addNew')}
        </Link>
      }
      isLoading={currenciesQuery.isLoading}
      loadingMessage={t('list.loading')}
      isError={currenciesQuery.isError}
      errorMessage={t('list.errorHeading')}
      errorDetail={(currenciesQuery.error as Error)?.message}
      onRetry={() => {
        void currenciesQuery.refetch();
      }}
    >
      <DataTable
        data={currenciesQuery.data ?? []}
        rowKey={(currency, index) => `currency-${currency.id ?? currency.code ?? index}`}
        title={t('list.tableTitle')}
        columns={columns}
        emptyMessage={t('list.empty')}
        mobileCardEyebrow={t('list.mobileEyebrow')}
      />
    </EntityListShell>
  );
};
