import { useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchCourierAllocationsForCurrentDay, type CourierAllocation } from '../api/courierAllocationsApi.ts';

type UseCourierAllocationsResult = {
  allocations: CourierAllocation[];
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => Promise<void>;
};

const toErrorMessage = (error: unknown): string => {
  if (typeof error === 'object' && error !== null && 'error' in error) {
    const responseError = (error as { error?: { message?: string } }).error?.message;
    if (responseError) {
      return responseError;
    }
  }

  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message;
  }

  return 'Nem sikerult betolteni a mai kiosztott csomagokat. Probald ujra.';
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
    errorMessage: allocationsQuery.isError ? toErrorMessage(allocationsQuery.error) : null,
    retry,
  };
};
