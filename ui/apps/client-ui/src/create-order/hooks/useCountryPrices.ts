import { useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchCountryPrices, type CountryPriceOption } from '../api/ordersApi.ts';
import { queryKeys } from '../../shared/queryKeys.ts';

type UseCountryPricesResult = {
  countryPrices: CountryPriceOption[];
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => void;
};

export const useCountryPrices = (
  originCountryId: string,
  destinationCountryId: string,
): UseCountryPricesResult => {
  const isEnabled = Boolean(originCountryId && destinationCountryId);
  const countryPricesQuery = useQuery({
    queryKey: queryKeys.countryPrices(originCountryId, destinationCountryId),
    queryFn: ({ signal }) => fetchCountryPrices(originCountryId, destinationCountryId, signal),
    enabled: isEnabled,
    staleTime: 60 * 1000,
    retry: 1,
  });

  const retry = useCallback(() => {
    if (!isEnabled) {
      return;
    }
    void countryPricesQuery.refetch();
  }, [countryPricesQuery, isEnabled]);

  return {
    countryPrices: isEnabled ? (countryPricesQuery.data ?? []) : [],
    isLoading: isEnabled ? countryPricesQuery.isPending : false,
    errorMessage: isEnabled && countryPricesQuery.isError
      ? 'Az adott orszagparhoz tartozo arak jelenleg nem erhetoek el.'
      : null,
    retry,
  };
};

