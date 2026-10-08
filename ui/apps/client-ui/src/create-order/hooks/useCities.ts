import { useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { fetchCityOptionsByCountryId, type CityOption } from '../api/ordersApi.ts';
import { queryKeys } from '../../shared/queryKeys.ts';

type UseCitiesResult = {
  cityOptions: CityOption[];
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => void;
};

export const useCities = (countryId: string): UseCitiesResult => {
  const { t } = useTranslation('createOrder');
  const isEnabled = Boolean(countryId);
  const citiesQuery = useQuery({
    queryKey: queryKeys.cities(countryId),
    queryFn: ({ signal }) => fetchCityOptionsByCountryId(countryId, signal),
    enabled: isEnabled,
    staleTime: 5 * 60 * 1000,
    retry: 2,
  });

  const retry = useCallback(() => {
    if (!isEnabled) {
      return;
    }
    void citiesQuery.refetch();
  }, [citiesQuery, isEnabled]);

  return {
    cityOptions: isEnabled ? (citiesQuery.data ?? []) : [],
    isLoading: isEnabled ? citiesQuery.isPending : false,
    errorMessage: isEnabled && citiesQuery.isError ? t('dataErrors.cities') : null,
    retry,
  };
};


