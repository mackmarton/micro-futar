import { useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import type { ShipmentRouteCourierDTO } from '@package/shared-core/api/CourierApiClient';
import { toErrorMessage } from '@package/shared-core';
import { fetchCourierPickupsForToday } from '../api/courierPickupApi.ts';

type UseCourierPickupsResult = {
  assignments: ShipmentRouteCourierDTO[];
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => Promise<void>;
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
    errorMessage: pickupsQuery.isError
      ? toErrorMessage(pickupsQuery.error, 'Nem sikerült betölteni a mai felvételi listát. Próbáld újra.')
      : null,
    retry,
  };
};
