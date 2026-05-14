import { useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import type { ShipmentRouteCourierDTO } from '@package/shared-core/api/CourierApiClient';
import { fetchCourierPickupsForToday } from '../api/courierPickupApi.ts';

type UseCourierPickupsResult = {
  assignments: ShipmentRouteCourierDTO[];
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

  return 'Nem sikerult betolteni a mai felveteli listat. Probald ujra.';
};

export const useCourierPickups = (): UseCourierPickupsResult => {
  const pickupsQuery = useQuery({
    queryKey: ['courier-pickups-today'],
    queryFn: ({ signal }) => fetchCourierPickupsForToday(signal),
    retry: 1,
  });

  const retry = useCallback(async () => {
    await pickupsQuery.refetch();
  }, [pickupsQuery]);

  return {
    assignments: pickupsQuery.data ?? [],
    isLoading: pickupsQuery.isPending,
    errorMessage: pickupsQuery.isError ? toErrorMessage(pickupsQuery.error) : null,
    retry,
  };
};
