import { useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchPackageSizeOptions, type PackageSizeOption } from '../api/ordersApi.ts';
import { queryKeys } from '../../shared/queryKeys.ts';

type UsePackageSizesResult = {
  packageSizeOptions: PackageSizeOption[];
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => void;
};

export const usePackageSizes = (): UsePackageSizesResult => {
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
    errorMessage: packageSizesQuery.isError ? 'A csomagmeretek listaja jelenleg nem erheto el.' : null,
    retry,
  };
};

