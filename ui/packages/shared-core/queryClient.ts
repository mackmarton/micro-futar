import { MutationCache, QueryCache, QueryClient } from '@tanstack/react-query';
import { isRateLimitedError, toRateLimitMessage } from './errors';

// Sok felület közvetlenül az `error.message`-et írja ki, ezért a 429-es hibára itt, egy helyen kerül rá az üzenet.
const describeRateLimitError = (error: unknown) => {
  const message = toRateLimitMessage(error);
  if (message) {
    Object.assign(error as object, { message });
  }
};

// 429 után az azonnali újrapróbálás is elutasításra futna.
export const retryUnlessRateLimited =
  (maxRetries: number) =>
  (failureCount: number, error: unknown): boolean =>
    !isRateLimitedError(error) && failureCount < maxRetries;

export const createAppQueryClient = () =>
  new QueryClient({
    queryCache: new QueryCache({ onError: describeRateLimitError }),
    mutationCache: new MutationCache({ onError: describeRateLimitError }),
    defaultOptions: {
      queries: {
        retry: retryUnlessRateLimited(2),
        staleTime: 30_000,
        refetchOnWindowFocus: false,
      },
    },
  });
