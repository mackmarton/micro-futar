import { buildApiUrl, resolveApiBaseUrl } from '@package/shared-core';
import {
  Api,
  type DepoDTO,
  type LocationCityDTO,
  type LocationCountryDTO,
} from '@package/shared-core/api/LogisticsApiClient';

export type DepoWithLookups = DepoDTO & {
  cityName?: string;
  countryName?: string;
};

const logisticsApi = new Api({
  baseUrl: resolveApiBaseUrl(),
  baseApiParams: {
    credentials: 'include',
  },
});

export const getAllDepos = async (): Promise<DepoDTO[]> => {
  try {
    const response = await logisticsApi.api.getAllDepos({ format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getAllCountries = async (): Promise<LocationCountryDTO[]> => {
  try {
    const response = await logisticsApi.api.getAllCountries({ format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getAllCities = async (): Promise<LocationCityDTO[]> => {
  try {
    const response = await logisticsApi.api.getAllCities({ format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

const getCityNameById = async (cityId: number): Promise<string | undefined> => {
  const response = await logisticsApi.api.getCityById(cityId, { format: 'json' });
  return response.data?.name;
};

const getCountryNameById = async (countryId: number): Promise<string | undefined> => {
  const response = await logisticsApi.api.getCountryById(countryId, { format: 'json' });
  return response.data?.name;
};

const enrichDepo = async (
  depo: DepoDTO,
  cityNameCache: Map<number, Promise<string | undefined>>,
  countryNameCache: Map<number, Promise<string | undefined>>,
): Promise<DepoWithLookups> => {
  const cityId = depo.locationCityId;
  const countryId = depo.locationCountryId;

  if (typeof cityId === 'number' && !cityNameCache.has(cityId)) {
    cityNameCache.set(cityId, getCityNameById(cityId));
  }

  if (typeof countryId === 'number' && !countryNameCache.has(countryId)) {
    countryNameCache.set(countryId, getCountryNameById(countryId));
  }

  return {
    ...depo,
    cityName: typeof cityId === 'number' ? await cityNameCache.get(cityId) : undefined,
    countryName: typeof countryId === 'number' ? await countryNameCache.get(countryId) : undefined,
  };
};

export const getAllDeposWithLookups = async (): Promise<DepoWithLookups[]> => {
  try {
    const depos = await getAllDepos();

    const cityNameCache = new Map<number, Promise<string | undefined>>();
    const countryNameCache = new Map<number, Promise<string | undefined>>();

    return Promise.all(depos.map((depo) => enrichDepo(depo, cityNameCache, countryNameCache)));
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getDepoByIdWithLookups = async (depoId: number): Promise<DepoWithLookups> => {
  try {
    const response = await logisticsApi.api.getDepoById(depoId, { format: 'json' });
    const depo = response.data;

    if (!depo) {
      throw new Error('Depo nem található.');
    }

    return enrichDepo(depo, new Map(), new Map());
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const createDepo = async (depo: DepoDTO): Promise<DepoDTO> => {
  try {
    const response = await logisticsApi.api.createDepo(depo, { format: 'json' });
    return response.data ?? depo;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const updateDepo = async (depoId: number, depo: DepoDTO): Promise<DepoDTO> => {
  try {
    const response = await logisticsApi.api.updateDepo(depoId, depo, { format: 'json' });
    return response.data ?? depo;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

