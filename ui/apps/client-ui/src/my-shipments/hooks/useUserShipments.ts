import { useCallback, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import type { Shipment } from '../components/ShipmentTable.tsx';
import type { ShipmentStatsObject } from '../components/ShipmentStats.tsx';
import { fetchShipmentsForUser } from '../api/shipmentsApi.ts';
import { buildShipmentStats, mapShipmentDtosToShipments } from '../mappers/shipmentMapper.ts';
import { queryKeys } from '../../shared/queryKeys.ts';

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
  const shipmentsQuery = useQuery({
    queryKey: queryKeys.userShipments,
    queryFn: ({ signal }) => fetchShipmentsForUser(signal),
    retry: 1,
  });

  const shipments = useMemo<Shipment[]>(() => {
    if (!shipmentsQuery.data) {
      return [];
    }

    return mapShipmentDtosToShipments(shipmentsQuery.data);
  }, [shipmentsQuery.data]);

  const stats = useMemo<ShipmentStatsObject>(() => buildShipmentStats(shipments), [shipments]);

  const errorMessage = shipmentsQuery.isError ? toErrorMessage(shipmentsQuery.error) : null;

  const retry = useCallback(async () => {
    await shipmentsQuery.refetch();
  }, [shipmentsQuery]);

  return {
    shipments,
    stats: errorMessage ? EMPTY_STATS : stats,
    isLoading: shipmentsQuery.isPending,
    errorMessage,
    retry,
  };
};

