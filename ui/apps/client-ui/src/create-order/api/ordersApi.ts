import { Api, type LocationCountryDTO } from '@package/shared-core/api/OrdersApiClient';

export type CountryOption = {
  value: string;
  label: string;
};

type RuntimeEnv = {
  VITE_API_BASE_URL?: string;
  API_BASE_URL?: string;
};

const resolveApiBaseUrl = () => {
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

const ordersApiClient = new Api({
  baseUrl: resolveApiBaseUrl(),
  baseApiParams: {
    credentials: 'include',
  },
});

const isValidCountry = (
  country: LocationCountryDTO,
): country is Required<Pick<LocationCountryDTO, 'id' | 'name'>> => {
  return typeof country.id === 'number' && typeof country.name === 'string' && country.name.trim().length > 0;
};

export const fetchCountryOptions = async (signal?: AbortSignal): Promise<CountryOption[]> => {
  const response = await ordersApiClient.api.getAllCountries({
    signal,
    format: 'json',
  });

  return (response.data ?? [])
    .filter(isValidCountry)
    .map((country) => ({
      value: String(country.id),
      label: country.name,
    }))
    .sort((left, right) => left.label.localeCompare(right.label, 'hu'));
};

