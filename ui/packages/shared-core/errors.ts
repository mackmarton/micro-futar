export const toErrorMessage = (error: unknown, fallback: string): string => {
  if (typeof error === 'object' && error !== null && 'error' in error) {
    const responseError = (error as { error?: { message?: string } }).error?.message;
    if (responseError) {
      return responseError;
    }
  }

  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message;
  }

  return fallback;
};

// A generált kliens a hibás választ `{ status, error: <törzs> }` alakban dobja; a kódot a backend
// ProblemDetail-je adja. Csak `format: 'json'` hívásoknál van feldolgozott törzs.
export const isEmailNotVerifiedError = (error: unknown): boolean =>
  (error as { error?: { code?: unknown } } | null | undefined)?.error?.code === 'EMAIL_NOT_VERIFIED';
