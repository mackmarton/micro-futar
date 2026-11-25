const RATE_LIMIT_MESSAGES = {
  hu: 'Túl sok kérés érkezett. Kérjük, próbáld újra néhány perc múlva.',
  en: 'Too many requests. Please try again in a few minutes.',
};

// A generált kliens a válaszobjektumot dobja (`status`), az axios pedig `response.status`-t ad.
export const isRateLimitedError = (error: unknown): boolean => {
  const candidate = error as { status?: unknown; response?: { status?: unknown } } | null | undefined;
  return candidate?.status === 429 || candidate?.response?.status === 429;
};

// A 429-es válasz fordított üzenete, más hibánál `null`. A nyelv a `<html lang>`-ból jön, amit minden app beállít.
export const toRateLimitMessage = (error: unknown): string | null => {
  if (!isRateLimitedError(error)) {
    return null;
  }

  const isEnglish = typeof document !== 'undefined' && document.documentElement.lang.startsWith('en');
  return isEnglish ? RATE_LIMIT_MESSAGES.en : RATE_LIMIT_MESSAGES.hu;
};

export const toErrorMessage = (error: unknown, fallback: string): string => {
  const rateLimitMessage = toRateLimitMessage(error);
  if (rateLimitMessage) {
    return rateLimitMessage;
  }

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
