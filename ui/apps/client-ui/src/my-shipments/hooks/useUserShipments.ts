import { useCallback, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { toErrorMessage } from '@package/shared-core';
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

export const useUserShipments = (): UseUserShipmentsResult => {
  const { t } = useTranslation('dashboard');
  const shipmentsQuery = useQuery({
    queryKey: queryKeys.userShipments,
    queryFn: ({ signal }) => fetchShipmentsForUser(signal),
    retry: 1,
  });

  const shipments = useMemo<Shipment[]>(() => {
    if (!shipmentsQuery.data) {
      return [];
    }

    return mapShipmentDtosToShipments(shipmentsQuery.data, t('unknownDestination'));
  }, [shipmentsQuery.data, t]);

  const stats = useMemo<ShipmentStatsObject>(() => buildShipmentStats(shipments), [shipments]);

  const errorMessage = shipmentsQuery.isError
    ? toErrorMessage(shipmentsQuery.error, t('error.generic'))
    : null;

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

