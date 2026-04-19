export type RuntimeEnv = {
  VITE_API_BASE_URL?: string;
  API_BASE_URL?: string;
};

export const resolveApiBaseUrl = (): string => {
  const globalEnv = ((globalThis as { __APP_ENV__?: RuntimeEnv }).__APP_ENV__) ?? {};
  const processEnv = ((globalThis as { process?: { env?: RuntimeEnv } }).process?.env ?? {});

  return (
    globalEnv.VITE_API_BASE_URL ??
    globalEnv.API_BASE_URL ??
    processEnv.VITE_API_BASE_URL ??
    processEnv.API_BASE_URL ??
    'http://localhost:8085'
  );
};

export const buildApiUrl = (path: string): string => {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;

  try {
    return new URL(normalizedPath, resolveApiBaseUrl()).toString();
  } catch {
    return `${resolveApiBaseUrl().replace(/\/$/, '')}${normalizedPath}`;
  }
};

