import { useCallback, useEffect, useState } from 'react';
import { fetchCountryOptions, type CountryOption } from '../api/ordersApi.ts';

type UseCountriesResult = {
  countryOptions: CountryOption[];
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => void;
};

export const useCountries = (): UseCountriesResult => {
  const [countryOptions, setCountryOptions] = useState<CountryOption[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [requestVersion, setRequestVersion] = useState(0);

  const retry = useCallback(() => {
    setRequestVersion((previous) => previous + 1);
  }, []);

  useEffect(() => {
    const abortController = new AbortController();

    const loadCountries = async () => {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const nextCountryOptions = await fetchCountryOptions(abortController.signal);
        setCountryOptions(nextCountryOptions);
      } catch (error) {
        if (abortController.signal.aborted) {
          return;
        }

        setCountryOptions([]);
        setErrorMessage('Az országok listája jelenleg nem érhető el.');
        console.error('Failed to load countries for address form.', error);
      } finally {
        if (!abortController.signal.aborted) {
          setIsLoading(false);
        }
      }
    };

    void loadCountries();

    return () => {
      abortController.abort();
    };
  }, [requestVersion]);

  return {
    countryOptions,
    isLoading,
    errorMessage,
    retry,
  };
};

