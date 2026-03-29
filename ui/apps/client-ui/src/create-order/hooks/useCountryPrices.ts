import { useCallback, useEffect, useState } from 'react';
import { fetchCountryPrices, type CountryPriceOption } from '../api/ordersApi.ts';

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
  const [countryPrices, setCountryPrices] = useState<CountryPriceOption[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [requestVersion, setRequestVersion] = useState(0);

  const retry = useCallback(() => {
    setRequestVersion((previous) => previous + 1);
  }, []);

  useEffect(() => {
    if (!originCountryId || !destinationCountryId) {
      setCountryPrices([]);
      setErrorMessage(null);
      setIsLoading(false);
      return;
    }

    const abortController = new AbortController();

    const loadCountryPrices = async () => {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const nextCountryPrices = await fetchCountryPrices(originCountryId, destinationCountryId, abortController.signal);
        setCountryPrices(nextCountryPrices);
      } catch (error) {
        if (abortController.signal.aborted) {
          return;
        }

        setCountryPrices([]);
        setErrorMessage('Az adott orszagparhoz tartozo arak jelenleg nem erhetoek el.');
        console.error('Failed to load country prices for selected route.', error);
      } finally {
        if (!abortController.signal.aborted) {
          setIsLoading(false);
        }
      }
    };

    void loadCountryPrices();

    return () => {
      abortController.abort();
    };
  }, [destinationCountryId, originCountryId, requestVersion]);

  return {
    countryPrices,
    isLoading,
    errorMessage,
    retry,
  };
};

