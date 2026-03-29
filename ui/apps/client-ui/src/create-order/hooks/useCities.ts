import { useCallback, useEffect, useState } from 'react';
import { fetchCityOptionsByCountryId, type CityOption } from '../api/ordersApi.ts';

type UseCitiesResult = {
  cityOptions: CityOption[];
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => void;
};

export const useCities = (countryId: string): UseCitiesResult => {
  const [cityOptions, setCityOptions] = useState<CityOption[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [requestVersion, setRequestVersion] = useState(0);

  const retry = useCallback(() => {
    setRequestVersion((previous) => previous + 1);
  }, []);

  useEffect(() => {
    if (!countryId) {
      setCityOptions([]);
      setErrorMessage(null);
      setIsLoading(false);
      return;
    }

    const abortController = new AbortController();

    const loadCities = async () => {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const nextCityOptions = await fetchCityOptionsByCountryId(countryId, abortController.signal);
        setCityOptions(nextCityOptions);
      } catch (error) {
        if (abortController.signal.aborted) {
          return;
        }

        setCityOptions([]);
        setErrorMessage('A varosok listaja jelenleg nem erheto el.');
        console.error('Failed to load cities for address form.', error);
      } finally {
        if (!abortController.signal.aborted) {
          setIsLoading(false);
        }
      }
    };

    void loadCities();

    return () => {
      abortController.abort();
    };
  }, [countryId, requestVersion]);

  return {
    cityOptions,
    isLoading,
    errorMessage,
    retry,
  };
};


