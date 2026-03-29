import {Api, type LocationCountryDTO} from '@package/shared-core/api/OrdersApiClient';
import type {LocationCityDTO} from '@package/shared-core/api/OrdersApiClient';
import type {PackageSizeDTO} from '@package/shared-core/api/OrdersApiClient';
import type {CountryPriceDTO} from '@package/shared-core/api/OrdersApiClient';
import type {ShipmentDTO} from '@package/shared-core/api/OrdersApiClient';

export type CountryOption = {
    value: string;
    label: string;
};

export type CityOption = {
    value: string;
    label: string;
};

export type PackageSizeOption = {
    id: number;
    name: string;
    maxLength: number;
};

export type CountryPriceOption = {
    id: number;
    originCountryId: number;
    destinationCountryId: number;
    packageSizeId: number;
    minPrice: number;
    maxPrice: number;
};

export type CreateShipmentPayload = Required<Pick<
    ShipmentDTO,
    | 'senderName'
    | 'senderEmail'
    | 'senderPhone'
    | 'senderLocationCountryId'
    | 'senderZip'
    | 'senderLocationCityId'
    | 'senderAddress'
    | 'recipientName'
    | 'recipientEmail'
    | 'recipientPhone'
    | 'recipientLocationCountryId'
    | 'recipientZip'
    | 'recipientLocationCityId'
    | 'recipientAddress'
    | 'packageSizeId'
>>;

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

const isValidCity = (
    city: LocationCityDTO,
): city is Required<Pick<LocationCityDTO, 'id' | 'name' | 'countryId'>> => {
    return (
        typeof city.id === 'number' &&
        typeof city.name === 'string' &&
        city.name.trim().length > 0 &&
        typeof city.countryId === 'number'
    );
};

const isValidPackageSize = (
    packageSize: PackageSizeDTO,
): packageSize is Required<Pick<PackageSizeDTO, 'id' | 'name' | 'maxLength'>> => {
    return (
        typeof packageSize.id === 'number' &&
        typeof packageSize.name === 'string' &&
        typeof packageSize.maxLength === 'number' &&
        packageSize.name.trim().length > 0
    );
};

const isValidCountryPrice = (
    countryPrice: CountryPriceDTO,
): countryPrice is Required<Pick<CountryPriceDTO, 'id' | 'originCountryId' | 'destinationCountryId' | 'packageSizeId' | 'minPrice' | 'maxPrice'>> => {
    return (
        typeof countryPrice.id === 'number' &&
        typeof countryPrice.originCountryId === 'number' &&
        typeof countryPrice.destinationCountryId === 'number' &&
        typeof countryPrice.packageSizeId === 'number' &&
        typeof countryPrice.minPrice === 'number' &&
        typeof countryPrice.maxPrice === 'number'
    );
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

export const fetchCityOptionsByCountryId = async (
    countryId: string,
    signal?: AbortSignal,
): Promise<CityOption[]> => {
    const parsedCountryId = Number(countryId);

    if (!Number.isInteger(parsedCountryId)) {
        return [];
    }

    const response = await ordersApiClient.api.getAllCitiesByCountryId(parsedCountryId, {
        signal,
        format: 'json',
    });

    return (response.data ?? [])
        .filter(isValidCity)
        .map((city) => ({
            value: String(city.id),
            label: city.name,
        }))
        .sort((left, right) => left.label.localeCompare(right.label, 'hu'));
};

export const fetchPackageSizeOptions = async (signal?: AbortSignal): Promise<PackageSizeOption[]> => {
    const response = await ordersApiClient.api.getAllPackageSizes({
        signal,
        format: 'json',
    });

    return (response.data ?? [])
        .filter(isValidPackageSize)
        .map((packageSize) => ({
            id: packageSize.id,
            name: packageSize.name,
            maxLength: packageSize.maxLength,
        }))
        .sort((left, right) => left.maxLength > right.maxLength ? 1 : -1);
};

export const fetchCountryPrices = async (
    originCountryId: string,
    destinationCountryId: string,
    signal?: AbortSignal,
): Promise<CountryPriceOption[]> => {
    const parsedOriginCountryId = Number(originCountryId);
    const parsedDestinationCountryId = Number(destinationCountryId);

    if (!Number.isInteger(parsedOriginCountryId) || !Number.isInteger(parsedDestinationCountryId)) {
        return [];
    }

    const response = await ordersApiClient.api.getAllCountryPrices(
        {
            originCountryId: parsedOriginCountryId,
            destinationCountryId: parsedDestinationCountryId,
        },
        {
            signal,
            format: 'json',
        },
    );

    return (response.data ?? [])
        .filter(isValidCountryPrice)
        .map((countryPrice) => ({
            id: countryPrice.id,
            originCountryId: countryPrice.originCountryId,
            destinationCountryId: countryPrice.destinationCountryId,
            packageSizeId: countryPrice.packageSizeId,
            minPrice: countryPrice.minPrice,
            maxPrice: countryPrice.maxPrice,
        }));
};

export const createShipment = async (payload: CreateShipmentPayload): Promise<ShipmentDTO> => {
    const response = await ordersApiClient.api.newShipment(payload, {
        format: 'json',
    });

    return response.data;
};

