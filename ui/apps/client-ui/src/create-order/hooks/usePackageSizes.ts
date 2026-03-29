import { useCallback, useEffect, useState } from 'react';
import { fetchPackageSizeOptions, type PackageSizeOption } from '../api/ordersApi.ts';

type UsePackageSizesResult = {
  packageSizeOptions: PackageSizeOption[];
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => void;
};

export const usePackageSizes = (): UsePackageSizesResult => {
  const [packageSizeOptions, setPackageSizeOptions] = useState<PackageSizeOption[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [requestVersion, setRequestVersion] = useState(0);

  const retry = useCallback(() => {
    setRequestVersion((previous) => previous + 1);
  }, []);

  useEffect(() => {
    const abortController = new AbortController();

    const loadPackageSizes = async () => {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const nextPackageSizeOptions = await fetchPackageSizeOptions(abortController.signal);
        setPackageSizeOptions(nextPackageSizeOptions);
      } catch (error) {
        if (abortController.signal.aborted) {
          return;
        }

        setPackageSizeOptions([]);
        setErrorMessage('A csomagmeretek listaja jelenleg nem erheto el.');
        console.error('Failed to load package sizes for create order form.', error);
      } finally {
        if (!abortController.signal.aborted) {
          setIsLoading(false);
        }
      }
    };

    void loadPackageSizes();

    return () => {
      abortController.abort();
    };
  }, [requestVersion]);

  return {
    packageSizeOptions,
    isLoading,
    errorMessage,
    retry,
  };
};

