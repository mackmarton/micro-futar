import { buildApiUrl, resolveApiBaseUrl } from '@package/shared-core';
import {
  Api,
  type CourierDTO,
  type DepoDTO,
  type DepoTransitDTO,
  type LocationCityDTO,
  type LocationCountryDTO,
  type PackageSizeDTO,
  type VehicleDTO,
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

export const getVehicleRegistrationNumberById = async (vehicleId: number): Promise<string | undefined> => {
  try {
    const response = await logisticsApi.api.getVehicleById(vehicleId, { format: 'json' });
    return response.data?.registrationNumber;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
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

export const planShipmentsForDepo = async (depoId: number): Promise<Record<string, unknown>> => {
  try {
    const response = await logisticsApi.api.planShipmentsForDepo(depoId, { format: 'json' });
    return response.data ?? {};
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const planCrossDepoShipmentsForDepo = async (depoId: number): Promise<Record<string, unknown>> => {
  try {
    const response = await logisticsApi.api.planCrossDepoShipmentsForDepo(depoId, { format: 'json' });
    return response.data ?? {};
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getAllPackageSizes = async (): Promise<PackageSizeDTO[]> => {
  try {
    const response = await logisticsApi.api.getAllPackageSizes({ format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getDepoTransitsByOriginDepoId = async (originDepoId: number): Promise<DepoTransitDTO[]> => {
  try {
    const response = await logisticsApi.api.getDepoTransitsByOriginDepoId(originDepoId, { format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getDepoTransitById = async (depoTransitId: number): Promise<DepoTransitDTO> => {
  try {
    const response = await logisticsApi.api.getDepoTransitById(depoTransitId, { format: 'json' });

    if (!response.data) {
      throw new Error('Depó tranzit nem található.');
    }

    return response.data;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const createDepoTransit = async (depoTransit: DepoTransitDTO): Promise<DepoTransitDTO> => {
  try {
    const response = await logisticsApi.api.createDepoTransit(depoTransit, { format: 'json' });
    return response.data ?? depoTransit;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const updateDepoTransit = async (
  depoTransitId: number,
  depoTransit: DepoTransitDTO,
): Promise<DepoTransitDTO> => {
  try {
    const response = await logisticsApi.api.updateDepoTransit(depoTransitId, depoTransit, { format: 'json' });
    return response.data ?? depoTransit;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getDepoTransitsByDestinationDepoId = async (
  destinationDepoId: number,
): Promise<DepoTransitDTO[]> => {
  try {
    const response = await logisticsApi.api.getDepoTransitsByDestinationDepoId(destinationDepoId, { format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getCourierByDepoId = async (depoId: number): Promise<CourierDTO[]> => {
  try {
    const response = await logisticsApi.api.getCourierByDepoId(depoId, { format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getCrossDepoCouriers = async (): Promise<CourierDTO[]> => {
  try {
    const response = await logisticsApi.api.getCrossDepoCouriers({ format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getCourierById = async (courierId: number): Promise<CourierDTO> => {
  try {
    const response = await logisticsApi.api.getCourierById(courierId, { format: 'json' });

    if (!response.data) {
      throw new Error('Futár nem található.');
    }

    return response.data;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const createCourier = async (courier: CourierDTO): Promise<CourierDTO> => {
  try {
    const response = await logisticsApi.api.createCourier(courier, { format: 'json' });
    return response.data ?? courier;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const updateCourier = async (courierId: number, courier: CourierDTO): Promise<CourierDTO> => {
  try {
    const response = await logisticsApi.api.updateCourier(courierId, courier, { format: 'json' });
    return response.data ?? courier;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getAllVehicles = async (): Promise<VehicleDTO[]> => {
  try {
    const response = await logisticsApi.api.getAllVehicles({ format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

