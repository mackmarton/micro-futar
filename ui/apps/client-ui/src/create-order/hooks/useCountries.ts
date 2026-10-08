import { useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { fetchCountryOptions, type CountryOption } from '../api/ordersApi.ts';
import { queryKeys } from '../../shared/queryKeys.ts';

type UseCountriesResult = {
  countryOptions: CountryOption[];
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => void;
};

export const useCountries = (): UseCountriesResult => {
  const { t } = useTranslation('createOrder');
  const countriesQuery = useQuery({
    queryKey: queryKeys.countries,
    queryFn: ({ signal }) => fetchCountryOptions(signal),
    staleTime: 5 * 60 * 1000,
    retry: 2,
  });

  const retry = useCallback(() => {
    void countriesQuery.refetch();
  }, [countriesQuery]);

  return {
    countryOptions: countriesQuery.data ?? [],
    isLoading: countriesQuery.isPending,
    errorMessage: countriesQuery.isError ? t('dataErrors.countries') : null,
    retry,
  };
};

