import { useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { fetchPackageSizeOptions, type PackageSizeOption } from '../api/ordersApi.ts';
import { queryKeys } from '../../shared/queryKeys.ts';
import { retryUnlessRateLimited, toRateLimitMessage } from '@package/shared-core';

type UsePackageSizesResult = {
  packageSizeOptions: PackageSizeOption[];
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => void;
};

export const usePackageSizes = (): UsePackageSizesResult => {
  const { t } = useTranslation('createOrder');
  const packageSizesQuery = useQuery({
    queryKey: queryKeys.packageSizes,
    queryFn: ({ signal }) => fetchPackageSizeOptions(signal),
    staleTime: 5 * 60 * 1000,
    retry: retryUnlessRateLimited(2),
  });

  const retry = useCallback(() => {
    void packageSizesQuery.refetch();
  }, [packageSizesQuery]);

  return {
    packageSizeOptions: packageSizesQuery.data ?? [],
    isLoading: packageSizesQuery.isPending,
    errorMessage: packageSizesQuery.isError ? (toRateLimitMessage(packageSizesQuery.error) ?? t('dataErrors.packageSizes')) : null,
    retry,
  };
};

