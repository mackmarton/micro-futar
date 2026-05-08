import { buildApiUrl, resolveApiBaseUrl } from '@package/shared-core';
import {
  Api,
  type CourierDTO,
  type DepoDTO,
  type DepoTransitDTO,
  type LocationCityDTO,
  type LocationCountryDTO,
  type LocationRegionDTO,
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

export const getAllRegions = async (): Promise<LocationRegionDTO[]> => {
  try {
    const response = await logisticsApi.api.getAllRegions({ format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getRegionById = async (regionId: number): Promise<LocationRegionDTO> => {
  try {
    const response = await logisticsApi.api.getRegionById(regionId, { format: 'json' });

    if (!response.data) {
      throw new Error('Régió nem található.');
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

export const createRegion = async (region: LocationRegionDTO): Promise<LocationRegionDTO> => {
  try {
    const response = await logisticsApi.api.createRegion(region, { format: 'json' });
    return response.data ?? region;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const updateRegion = async (regionId: number, region: LocationRegionDTO): Promise<LocationRegionDTO> => {
  try {
    const response = await logisticsApi.api.updateRegion(regionId, region, { format: 'json' });
    return response.data ?? region;
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
    const response = await logisticsApi.api.getAllCountries(undefined, { format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getCountryById = async (countryId: number): Promise<LocationCountryDTO> => {
  try {
    const response = await logisticsApi.api.getCountryById(countryId, { format: 'json' });

    if (!response.data) {
      throw new Error('Ország nem található.');
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

export const createCountry = async (country: LocationCountryDTO): Promise<LocationCountryDTO> => {
  try {
    const response = await logisticsApi.api.createCountry(country, { format: 'json' });
    return response.data ?? country;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const updateCountry = async (countryId: number, country: LocationCountryDTO): Promise<LocationCountryDTO> => {
  try {
    const response = await logisticsApi.api.updateCountry(countryId, country, { format: 'json' });
    return response.data ?? country;
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
    const response = await logisticsApi.api.getAllCities(undefined, { format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getCityById = async (cityId: number): Promise<LocationCityDTO> => {
  try {
    const response = await logisticsApi.api.getCityById(cityId, { format: 'json' });

    if (!response.data) {
      throw new Error('Város nem található.');
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

export const createCity = async (city: LocationCityDTO): Promise<LocationCityDTO> => {
  try {
    const response = await logisticsApi.api.createCity(city, { format: 'json' });
    return response.data ?? city;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const updateCity = async (cityId: number, city: LocationCityDTO): Promise<LocationCityDTO> => {
  try {
    const response = await logisticsApi.api.updateCity(cityId, city, { format: 'json' });
    return response.data ?? city;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getCountriesByRegionId = async (regionId: number): Promise<LocationCountryDTO[]> => {
  try {
    const response = await logisticsApi.api.getAllCountries({ regionId }, { format: 'json' });
    return response.data ?? [];
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const getCitiesByCountryId = async (countryId: number): Promise<LocationCityDTO[]> => {
  try {
    const response = await logisticsApi.api.getAllCities({ countryId }, { format: 'json' });
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
  const country = await getCountryById(countryId);
  return country.name;
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

export const getPackageSizeById = async (packageSizeId: number): Promise<PackageSizeDTO> => {
  try {
    const response = await logisticsApi.api.getPackageSizeById(packageSizeId, { format: 'json' });

    if (!response.data) {
      throw new Error('Csomagméret nem található.');
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

export const createPackageSize = async (packageSize: PackageSizeDTO): Promise<PackageSizeDTO> => {
  try {
    const response = await logisticsApi.api.createPackageSize(packageSize, { format: 'json' });
    return response.data ?? packageSize;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const updatePackageSize = async (
  packageSizeId: number,
  packageSize: PackageSizeDTO,
): Promise<PackageSizeDTO> => {
  try {
    const response = await logisticsApi.api.updatePackageSize(packageSizeId, packageSize, { format: 'json' });
    return response.data ?? packageSize;
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

export const getVehicleById = async (vehicleId: number): Promise<VehicleDTO> => {
  try {
    const response = await logisticsApi.api.getVehicleById(vehicleId, { format: 'json' });

    if (!response.data) {
      throw new Error('Jármű nem található.');
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

export const createVehicle = async (vehicle: VehicleDTO): Promise<VehicleDTO> => {
  try {
    const response = await logisticsApi.api.createVehicle(vehicle, { format: 'json' });
    return response.data ?? vehicle;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};

export const updateVehicle = async (vehicleId: number, vehicle: VehicleDTO): Promise<VehicleDTO> => {
  try {
    const response = await logisticsApi.api.updateVehicle(vehicleId, vehicle, { format: 'json' });
    return response.data ?? vehicle;
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};
