import { QueryClient } from '@tanstack/react-query';

export const createAppQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: 2,
        staleTime: 30_000,
        refetchOnWindowFocus: false,
      },
    },
  });
