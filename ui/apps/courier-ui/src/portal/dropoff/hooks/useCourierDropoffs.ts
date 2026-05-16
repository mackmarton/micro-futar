import { useCallback, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import type { ShipmentRouteCourierDTO } from '@package/shared-core/api/CourierApiClient';
import {
  countPendingDropoffsForAssignments,
  fetchCourierPickedUpAssignmentsForToday,
} from '../api/courierDropoffApi.ts';

type UseCourierDropoffsResult = {
  assignments: ShipmentRouteCourierDTO[];
  waitingDropoffCount: number;
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

  return 'Nem sikerult betolteni a mai leadasi listat. Probald ujra.';
};

export const useCourierDropoffs = (): UseCourierDropoffsResult => {
  const dropoffsQuery = useQuery({
    queryKey: ['courier-dropoffs-today'],
    queryFn: ({ signal }) => fetchCourierPickedUpAssignmentsForToday(signal),
    retry: 1,
  });
  const assignments = useMemo(
    () => dropoffsQuery.data ?? [],
    [dropoffsQuery.data],
  );
  const dropoffCountQueryKeySuffix = useMemo(
    () =>
      assignments.map((assignment, index) => ({
        id: assignment.id ?? index,
        shipmentRouteId: assignment.shipmentRouteId ?? null,
        pickedUpForDelivery: Boolean(assignment.pickedUpForDelivery),
        failed: Boolean(assignment.failed),
      })),
    [assignments],
  );
  const dropoffCountQuery = useQuery({
    queryKey: ['courier-dropoffs-pending-count', dropoffCountQueryKeySuffix],
    queryFn: ({ signal }) => countPendingDropoffsForAssignments(assignments, signal),
    enabled: assignments.length > 0,
    retry: 1,
  });

  const retry = useCallback(async () => {
    await Promise.all([dropoffsQuery.refetch(), dropoffCountQuery.refetch()]);
  }, [dropoffCountQuery, dropoffsQuery]);

  const waitingDropoffCount =
    assignments.length === 0
      ? 0
      : (dropoffCountQuery.data
          ?? assignments.filter((assignment) => !assignment.failed && assignment.pickedUpForDelivery).length);
  const isLoading = dropoffsQuery.isPending || (assignments.length > 0 && dropoffCountQuery.isPending);
  const errorMessage = dropoffsQuery.isError
    ? toErrorMessage(dropoffsQuery.error)
    : dropoffCountQuery.isError
      ? toErrorMessage(dropoffCountQuery.error)
      : null;

  return {
    assignments,
    waitingDropoffCount,
    isLoading,
    errorMessage,
    retry,
  };
};
