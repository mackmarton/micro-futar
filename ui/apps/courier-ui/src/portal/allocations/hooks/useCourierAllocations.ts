import { useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { toErrorMessage } from '@package/shared-core';
import { fetchCourierAllocationsForCurrentDay, type CourierAllocation } from '../api/courierAllocationsApi.ts';

type UseCourierAllocationsResult = {
  allocations: CourierAllocation[];
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => Promise<void>;
};

export const useCourierAllocations = (): UseCourierAllocationsResult => {
  const allocationsQuery = useQuery({
    queryKey: ['courier-allocations-current-day'],
    queryFn: ({ signal }) => fetchCourierAllocationsForCurrentDay(signal),
    retry: 1,
  });

  const retry = useCallback(async () => {
    await allocationsQuery.refetch();
  }, [allocationsQuery]);

  return {
    allocations: allocationsQuery.data ?? [],
    isLoading: allocationsQuery.isPending,
    errorMessage: allocationsQuery.isError
      ? toErrorMessage(allocationsQuery.error, 'Nem sikerült betölteni a mai kiosztott csomagokat. Próbáld újra.')
      : null,
    retry,
  };
};
