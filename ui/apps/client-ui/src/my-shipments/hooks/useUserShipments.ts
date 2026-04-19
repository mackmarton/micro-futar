import { useCallback, useEffect, useRef, useState } from 'react';
import type { Shipment } from '../components/ShipmentTable.tsx';
import type { ShipmentStatsObject } from '../components/ShipmentStats.tsx';
import { fetchShipmentsForUser } from '../api/shipmentsApi.ts';
import { buildShipmentStats, mapShipmentDtosToShipments } from '../mappers/shipmentMapper.ts';

type UseUserShipmentsResult = {
  shipments: Shipment[];
  stats: ShipmentStatsObject;
  isLoading: boolean;
  errorMessage: string | null;
  retry: () => Promise<void>;
};

const EMPTY_STATS: ShipmentStatsObject = {
  inProgress: 0,
  delivered: 0,
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

  return 'Nem sikerult betolteni a kuldemenyeket. Probald ujra.';
};

export const useUserShipments = (): UseUserShipmentsResult => {
  const [shipments, setShipments] = useState<Shipment[]>([]);
  const [stats, setStats] = useState<ShipmentStatsObject>(EMPTY_STATS);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);
  const requestIdRef = useRef(0);

  const loadShipments = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);

    abortControllerRef.current?.abort();
    const abortController = new AbortController();
    abortControllerRef.current = abortController;

    const requestId = requestIdRef.current + 1;
    requestIdRef.current = requestId;

    try {
      const shipmentDtos = await fetchShipmentsForUser(abortController.signal);

      if (abortController.signal.aborted || requestId !== requestIdRef.current) {
        return;
      }

      const mappedShipments = mapShipmentDtosToShipments(shipmentDtos);
      setShipments(mappedShipments);
      setStats(buildShipmentStats(mappedShipments));
    } catch (error) {
      if (abortController.signal.aborted || requestId !== requestIdRef.current) {
        return;
      }

      setShipments([]);
      setStats(EMPTY_STATS);
      setErrorMessage(toErrorMessage(error));
    } finally {
      if (requestId === requestIdRef.current) {
        setIsLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    void loadShipments();

    return () => {
      abortControllerRef.current?.abort();
    };
  }, [loadShipments]);

  const retry = useCallback(async () => {
    await loadShipments();
  }, [loadShipments]);

  return {
    shipments,
    stats,
    isLoading,
    errorMessage,
    retry,
  };
};

