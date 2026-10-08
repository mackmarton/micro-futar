import { useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { fetchPackageSizeOptions, type PackageSizeOption } from '../api/ordersApi.ts';
import { queryKeys } from '../../shared/queryKeys.ts';

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
    retry: 2,
  });

  const retry = useCallback(() => {
    void packageSizesQuery.refetch();
  }, [packageSizesQuery]);

  return {
    packageSizeOptions: packageSizesQuery.data ?? [],
    isLoading: packageSizesQuery.isPending,
    errorMessage: packageSizesQuery.isError ? t('dataErrors.packageSizes') : null,
    retry,
  };
};

