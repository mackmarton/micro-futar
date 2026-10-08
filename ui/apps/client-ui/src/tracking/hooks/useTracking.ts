import { useCallback, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { toErrorMessage } from '@package/shared-core';
import { fetchTrackingByParcelNumber } from '../api/trackingApi.ts';
import { mapTrackingDtoToDetails, type TrackingDetailsViewModel } from '../mappers/trackingMapper.ts';
import type { AppLocale } from '../../i18n/createI18nInstance.ts';
import { queryKeys } from '../../shared/queryKeys.ts';

type UseTrackingResult = {
  hasSearchStarted: boolean;
  isLoading: boolean;
  errorMessage: string | null;
  details: TrackingDetailsViewModel | null;
  search: (trackingNumber: string) => Promise<void>;
  retry: () => Promise<void>;
};

export const useTracking = (): UseTrackingResult => {
  const queryClient = useQueryClient();
  const { t, i18n } = useTranslation('tracking');
  const [hasSearchStarted, setHasSearchStarted] = useState(false);
  const [lastTrackingNumber, setLastTrackingNumber] = useState<string | null>(null);

  const trackingSearchMutation = useMutation({
    mutationFn: async (trackingNumber: string) => {
      const normalizedTrackingNumber = trackingNumber.trim();
      const details = await queryClient.fetchQuery({
        queryKey: queryKeys.tracking(normalizedTrackingNumber),
        queryFn: async ({ signal }) => {
          const trackingDto = await fetchTrackingByParcelNumber(normalizedTrackingNumber, signal);
          const mappedDetails = mapTrackingDtoToDetails(trackingDto, normalizedTrackingNumber, {
            t,
            locale: i18n.language as AppLocale,
          });

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

  const { mutateAsync } = trackingSearchMutation;

  // A `mutateAsync` stabil, a teljes mutation-objektum renderenként új, így csak ez szerepelhet a függőségben,
  // különben a `search` minden renderelésnél újraképződik, és a `useEffect`-ből hívott keresés végtelen ciklust indít.
  const search = useCallback(async (trackingNumber: string) => {
    const normalizedTrackingNumber = trackingNumber.trim();

    if (!normalizedTrackingNumber) {
      return;
    }

    setHasSearchStarted(true);
    setLastTrackingNumber(normalizedTrackingNumber);

    try {
      await mutateAsync(normalizedTrackingNumber);
    } catch {
      // Mutation state already exposes the error message.
    }
  }, [mutateAsync]);

  const retry = useCallback(async () => {
    if (!lastTrackingNumber) {
      return;
    }

    await search(lastTrackingNumber);
  }, [lastTrackingNumber, search]);

  const errorMessage = trackingSearchMutation.isError
    ? trackingSearchMutation.error instanceof Error && trackingSearchMutation.error.message === 'NO_TRACKING_RESULT'
      ? t('errors.notFound')
      : toErrorMessage(trackingSearchMutation.error, t('errors.generic'))
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

