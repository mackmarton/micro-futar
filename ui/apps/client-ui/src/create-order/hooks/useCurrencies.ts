import { useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { fetchCurrencyOptions, type CurrencyOption } from '../api/ordersApi.ts';
import { queryKeys } from '../../shared/queryKeys.ts';

type UseCurrenciesResult = {
  currencyOptions: CurrencyOption[];
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => void;
};

export const useCurrencies = (): UseCurrenciesResult => {
  const { t } = useTranslation('createOrder');
  const currenciesQuery = useQuery({
    queryKey: queryKeys.currencies,
    queryFn: ({ signal }) => fetchCurrencyOptions(signal),
    staleTime: 5 * 60 * 1000,
    retry: 2,
  });

  const retry = useCallback(() => {
    void currenciesQuery.refetch();
  }, [currenciesQuery]);

  return {
    currencyOptions: currenciesQuery.data ?? [],
    isLoading: currenciesQuery.isPending,
    errorMessage: currenciesQuery.isError ? t('dataErrors.currencies') : null,
    retry,
  };
};
