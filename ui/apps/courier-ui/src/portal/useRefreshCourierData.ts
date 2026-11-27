import { useCallback } from 'react';
import { useQueryClient } from '@tanstack/react-query';

/**
 * Marks all cached data stale after a courier action. Every list, count and manifest is derived from the same
 * assignments, and some of their query keys do not change when only the server side details do.
 */
export const useRefreshCourierData = (): (() => Promise<void>) => {
  const queryClient = useQueryClient();
  return useCallback(() => queryClient.invalidateQueries(), [queryClient]);
};
