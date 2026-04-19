import { useCallback, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchTrackingByParcelNumber } from '../api/trackingApi.ts';
import { mapTrackingDtoToDetails, type TrackingDetailsViewModel } from '../mappers/trackingMapper.ts';
import { queryKeys } from '../../shared/queryKeys.ts';

type UseTrackingResult = {
  hasSearchStarted: boolean;
  isLoading: boolean;
  errorMessage: string | null;
  details: TrackingDetailsViewModel | null;
  search: (trackingNumber: string) => Promise<void>;
  retry: () => Promise<void>;
};

const toErrorMessage = (error: unknown) => {
  if (typeof error === 'object' && error !== null && 'error' in error) {
    const responseError = (error as { error?: { message?: string } }).error?.message;
    if (responseError) {
      return responseError;
    }
  }

  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message;
  }

  return 'Nem sikerült betölteni a követési adatokat. Próbáld újra.';
};

export const useTracking = (): UseTrackingResult => {
  const queryClient = useQueryClient();
  const [hasSearchStarted, setHasSearchStarted] = useState(false);
  const [lastTrackingNumber, setLastTrackingNumber] = useState<string | null>(null);

  const trackingSearchMutation = useMutation({
    mutationFn: async (trackingNumber: string) => {
      const normalizedTrackingNumber = trackingNumber.trim();
      const details = await queryClient.fetchQuery({
        queryKey: queryKeys.tracking(normalizedTrackingNumber),
        queryFn: async ({ signal }) => {
          const trackingDto = await fetchTrackingByParcelNumber(normalizedTrackingNumber, signal);
          const mappedDetails = mapTrackingDtoToDetails(trackingDto, normalizedTrackingNumber);

          if (!mappedDetails) {
            throw new Error('NO_TRACKING_RESULT');
          }

          return mappedDetails;
        },
        staleTime: 30 * 1000,
        retry: 1,
      });

      return details;
    },
  });

  const search = useCallback(async (trackingNumber: string) => {
    const normalizedTrackingNumber = trackingNumber.trim();

    if (!normalizedTrackingNumber) {
      return;
    }

    setHasSearchStarted(true);
    setLastTrackingNumber(normalizedTrackingNumber);

    try {
      await trackingSearchMutation.mutateAsync(normalizedTrackingNumber);
    } catch {
      // Mutation state already exposes the error message.
    }
  }, [trackingSearchMutation]);

  const retry = useCallback(async () => {
    if (!lastTrackingNumber) {
      return;
    }

    await search(lastTrackingNumber);
  }, [lastTrackingNumber, search]);

  const errorMessage = trackingSearchMutation.isError
    ? trackingSearchMutation.error instanceof Error && trackingSearchMutation.error.message === 'NO_TRACKING_RESULT'
      ? 'Nincs találat erre a csomagszámra. Ellenőrizd és próbáld újra.'
      : toErrorMessage(trackingSearchMutation.error)
    : null;

  return {
    hasSearchStarted,
    isLoading: trackingSearchMutation.isPending,
    errorMessage,
    details: trackingSearchMutation.data ?? null,
    search,
    retry,
  };
};

