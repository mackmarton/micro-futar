import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { DataTable } from '@package/shared-ui';
import type { DataTableColumn } from '@package/shared-ui';
import type { CurrencyDTO } from '@package/shared-core/api/LogisticsApiClient';
import { Link } from 'react-router-dom';
import { getAllCurrencies } from '../api/logisticsDeposApi';
import { logisticsNavigationItems } from '../navigation';
import { EntityListShell } from '../shared/EntityListShell';

const valueOrFallback = (value?: string) => (typeof value === 'string' && value.length > 0 ? value : 'N/A');

export const LogisticsCurrenciesPage = () => {
  const currenciesQuery = useQuery({
    queryKey: ['logistics', 'currencies'],
    queryFn: getAllCurrencies,
  });

  const columns = useMemo<DataTableColumn<CurrencyDTO>[]>(
    () => [
      {
        id: 'code',
        header: 'Kód',
        mobileLabel: 'Kód',
        cell: (currency) => valueOrFallback(currency.code),
      },
      {
        id: 'name',
        header: 'Név',
        mobileLabel: 'Név',
        cell: (currency) => valueOrFallback(currency.name),
      },
      {
        id: 'symbol',
        header: 'Jel',
        mobileLabel: 'Jel',
        cell: (currency) => valueOrFallback(currency.symbol),
      },
      {
        id: 'edit',
        header: 'Szerkesztés',
        cell: (currency) =>
          typeof currency.id === 'number' ? (
            <Link
              to={`/portal/currencies/${currency.id}/edit`}
              className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
            >
              Szerkeszt
            </Link>
          ) : (
            <span className="text-on-surface-variant">N/A</span>
          ),
      },
    ],
    [],
  );

  return (
    <EntityListShell
      title="Pénznemek"
      activeHref="#/portal/currencies"
      navigationItems={logisticsNavigationItems}
      eyebrow="Logisztika"
      heading="Pénznemek"
      headerActions={
        <Link
          to="/portal/currencies/new"
          className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
        >
          Új pénznem létrehozása
        </Link>
      }
      isLoading={currenciesQuery.isLoading}
      loadingMessage="A pénznemek betöltése folyamatban..."
      isError={currenciesQuery.isError}
      errorMessage="Nem sikerült betölteni a pénznemeket."
      errorDetail={(currenciesQuery.error as Error)?.message}
      onRetry={() => {
        void currenciesQuery.refetch();
      }}
    >
      <DataTable
        data={currenciesQuery.data ?? []}
        rowKey={(currency, index) => `currency-${currency.id ?? currency.code ?? index}`}
        title="Pénznem lista"
        columns={columns}
        emptyMessage="Nincs elérhető pénznem rekord."
        mobileCardEyebrow="Pénznem"
      />
    </EntityListShell>
  );
};
