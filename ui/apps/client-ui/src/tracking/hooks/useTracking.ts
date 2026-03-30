import { useCallback, useEffect, useRef, useState } from 'react';
import { fetchTrackingByParcelNumber } from '../api/trackingApi.ts';
import { mapTrackingDtoToDetails, type TrackingDetailsViewModel } from '../mappers/trackingMapper.ts';

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
  const [hasSearchStarted, setHasSearchStarted] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [details, setDetails] = useState<TrackingDetailsViewModel | null>(null);
  const [lastTrackingNumber, setLastTrackingNumber] = useState<string | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);
  const requestIdRef = useRef(0);

  const search = useCallback(async (trackingNumber: string) => {
    const normalizedTrackingNumber = trackingNumber.trim();

    if (!normalizedTrackingNumber) {
      return;
    }

    setHasSearchStarted(true);
    setLastTrackingNumber(normalizedTrackingNumber);
    setIsLoading(true);
    setErrorMessage(null);

    abortControllerRef.current?.abort();
    const abortController = new AbortController();
    abortControllerRef.current = abortController;

    const requestId = requestIdRef.current + 1;
    requestIdRef.current = requestId;

    try {
      const trackingDto = await fetchTrackingByParcelNumber(normalizedTrackingNumber, abortController.signal);

      if (requestId !== requestIdRef.current) {
        return;
      }

      const mappedDetails = mapTrackingDtoToDetails(trackingDto, normalizedTrackingNumber);

      if (!mappedDetails) {
        setDetails(null);
        setErrorMessage('Nincs találat erre a csomagszámra. Ellenőrizd és próbáld újra.');
        return;
      }

      setDetails(mappedDetails);
    } catch (error) {
      if (abortController.signal.aborted || requestId !== requestIdRef.current) {
        return;
      }

      setDetails(null);
      setErrorMessage(toErrorMessage(error));
    } finally {
      if (requestId === requestIdRef.current) {
        setIsLoading(false);
      }
    }
  }, []);

  const retry = useCallback(async () => {
    if (!lastTrackingNumber) {
      return;
    }

    await search(lastTrackingNumber);
  }, [lastTrackingNumber, search]);

  useEffect(() => {
    return () => {
      abortControllerRef.current?.abort();
    };
  }, []);

  return {
    hasSearchStarted,
    isLoading,
    errorMessage,
    details,
    search,
    retry,
  };
};

