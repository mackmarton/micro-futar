/* eslint-disable */
/* tslint:disable */
// @ts-nocheck
/*
 * ---------------------------------------------------------------
 * ## THIS FILE WAS GENERATED VIA SWAGGER-TYPESCRIPT-API        ##
 * ##                                                           ##
 * ## AUTHOR: acacode                                           ##
 * ## SOURCE: https://github.com/acacode/swagger-typescript-api ##
 * ---------------------------------------------------------------
 */

export interface VehicleDTO {
  /** @format int64 */
  id?: number;
  registrationNumber?: string;
  /** @format double */
  maximumPackableVolume?: number;
}

export interface LocationRegionDTO {
  /** @format int64 */
  id?: number;
  name?: string;
}

export interface PackageSizeDTO {
  /** @format int64 */
  id?: number;
  name?: string;
  /** @format double */
  maxLength?: number;
}

export interface DepoDTO {
  /** @format int64 */
  id?: number;
  name?: string;
  /** @format int64 */
  locationCountryId?: number;
  zip?: string;
  /** @format int64 */
  locationCityId?: number;
  address?: string;
  /** @format double */
  latitude?: number;
  /** @format double */
  longitude?: number;
  mainDepo?: boolean;
}

export interface DepoTransitDTO {
  /** @format int64 */
  id?: number;
  /** @format int64 */
  originDepoId?: number;
  /** @format int64 */
  destinationDepoId?: number;
  /** @format int64 */
  packageSizeId?: number;
  transportType?: "ROAD" | "AIR";
  /** @format double */
  price?: number;
}

export interface CourierDTO {
  /** @format int64 */
  id?: number;
  name?: string;
  email?: string;
  telephone?: string;
  /** @format int64 */
  vehicleId?: number;
  qualifiedFor?: "ROAD" | "AIR";
  courierType?: "CROSS_DEPO" | "DELIVERY";
  /** @format int64 */
  depoId?: number;
}

export interface LocationCountryDTO {
  /** @format int64 */
  id?: number;
  /** @format int64 */
  regionId?: number;
  name?: string;
}

export interface LocationCityDTO {
  /** @format int64 */
  id?: number;
  /** @format int64 */
  countryId?: number;
  name?: string;
}

export interface ShipmentRouteDTO {
  /** @format int64 */
  id?: number;
  /** @format int64 */
  shipmentId?: number;
  /** @format int64 */
  originDepoId?: number;
  /** @format int64 */
  destinationDepoId?: number;
  originAddress?: string;
  destinationAddress?: string;
  /** @format int32 */
  routePartNumber?: number;
  /** @format date-time */
  fulfillmentTime?: string;
}

export interface CountryPriceDTO {
  /** @format int64 */
  id?: number;
  /** @format int64 */
  originCountryId?: number;
  /** @format int64 */
  destinationCountryId?: number;
  /** @format int64 */
  packageSizeId?: number;
  /** @format double */
  minPrice?: number;
  /** @format double */
  maxPrice?: number;
}

export type QueryParamsType = Record<string | number, any>;
export type ResponseFormat = keyof Omit<Body, "body" | "bodyUsed">;

export interface FullRequestParams extends Omit<RequestInit, "body"> {
  /** set parameter to `true` for call `securityWorker` for this request */
  secure?: boolean;
  /** request path */
  path: string;
  /** content type of request body */
  type?: ContentType;
  /** query params */
  query?: QueryParamsType;
  /** format of response (i.e. response.json() -> format: "json") */
  format?: ResponseFormat;
  /** request body */
  body?: unknown;
  /** base url */
  baseUrl?: string;
  /** request cancellation token */
  cancelToken?: CancelToken;
}

export type RequestParams = Omit<
  FullRequestParams,
  "body" | "method" | "query" | "path"
>;

export interface ApiConfig<SecurityDataType = unknown> {
  baseUrl?: string;
  baseApiParams?: Omit<RequestParams, "baseUrl" | "cancelToken" | "signal">;
  securityWorker?: (
    securityData: SecurityDataType | null,
  ) => Promise<RequestParams | void> | RequestParams | void;
  customFetch?: typeof fetch;
}

export interface HttpResponse<D extends unknown, E extends unknown = unknown>
  extends Response {
  data: D;
  error: E;
}

type CancelToken = Symbol | string | number;

export enum ContentType {
  Json = "application/json",
  JsonApi = "application/vnd.api+json",
  FormData = "multipart/form-data",
  UrlEncoded = "application/x-www-form-urlencoded",
  Text = "text/plain",
}

export class HttpClient<SecurityDataType = unknown> {
  public baseUrl: string = "http://localhost:8085";
  private securityData: SecurityDataType | null = null;
  private securityWorker?: ApiConfig<SecurityDataType>["securityWorker"];
  private abortControllers = new Map<CancelToken, AbortController>();
  private customFetch = (...fetchParams: Parameters<typeof fetch>) =>
    fetch(...fetchParams);

  private baseApiParams: RequestParams = {
    credentials: "same-origin",
    headers: {},
    redirect: "follow",
    referrerPolicy: "no-referrer",
  };

  constructor(apiConfig: ApiConfig<SecurityDataType> = {}) {
    Object.assign(this, apiConfig);
  }

  public setSecurityData = (data: SecurityDataType | null) => {
    this.securityData = data;
  };

  protected encodeQueryParam(key: string, value: any) {
    const encodedKey = encodeURIComponent(key);
    return `${encodedKey}=${encodeURIComponent(typeof value === "number" ? value : `${value}`)}`;
  }

  protected addQueryParam(query: QueryParamsType, key: string) {
    return this.encodeQueryParam(key, query[key]);
  }

  protected addArrayQueryParam(query: QueryParamsType, key: string) {
    const value = query[key];
    return value.map((v: any) => this.encodeQueryParam(key, v)).join("&");
  }

  protected toQueryString(rawQuery?: QueryParamsType): string {
    const query = rawQuery || {};
    const keys = Object.keys(query).filter(
      (key) => "undefined" !== typeof query[key],
    );
    return keys
      .map((key) =>
        Array.isArray(query[key])
          ? this.addArrayQueryParam(query, key)
          : this.addQueryParam(query, key),
      )
      .join("&");
  }

  protected addQueryParams(rawQuery?: QueryParamsType): string {
    const queryString = this.toQueryString(rawQuery);
    return queryString ? `?${queryString}` : "";
  }

  private contentFormatters: Record<ContentType, (input: any) => any> = {
    [ContentType.Json]: (input: any) =>
      input !== null && (typeof input === "object" || typeof input === "string")
        ? JSON.stringify(input)
        : input,
    [ContentType.JsonApi]: (input: any) =>
      input !== null && (typeof input === "object" || typeof input === "string")
        ? JSON.stringify(input)
        : input,
    [ContentType.Text]: (input: any) =>
      input !== null && typeof input !== "string"
        ? JSON.stringify(input)
        : input,
    [ContentType.FormData]: (input: any) => {
      if (input instanceof FormData) {
        return input;
      }

      return Object.keys(input || {}).reduce((formData, key) => {
        const property = input[key];
        formData.append(
          key,
          property instanceof Blob
            ? property
            : typeof property === "object" && property !== null
              ? JSON.stringify(property)
              : `${property}`,
        );
        return formData;
      }, new FormData());
    },
    [ContentType.UrlEncoded]: (input: any) => this.toQueryString(input),
  };

  protected mergeRequestParams(
    params1: RequestParams,
    params2?: RequestParams,
  ): RequestParams {
    return {
      ...this.baseApiParams,
      ...params1,
      ...(params2 || {}),
      headers: {
        ...(this.baseApiParams.headers || {}),
        ...(params1.headers || {}),
        ...((params2 && params2.headers) || {}),
      },
    };
  }

  protected createAbortSignal = (
    cancelToken: CancelToken,
  ): AbortSignal | undefined => {
    if (this.abortControllers.has(cancelToken)) {
      const abortController = this.abortControllers.get(cancelToken);
      if (abortController) {
        return abortController.signal;
      }
      return void 0;
    }

    const abortController = new AbortController();
    this.abortControllers.set(cancelToken, abortController);
    return abortController.signal;
  };

  public abortRequest = (cancelToken: CancelToken) => {
    const abortController = this.abortControllers.get(cancelToken);

    if (abortController) {
      abortController.abort();
      this.abortControllers.delete(cancelToken);
    }
  };

  public request = async <T = any, E = any>({
    body,
    secure,
    path,
    type,
    query,
    format,
    baseUrl,
    cancelToken,
    ...params
  }: FullRequestParams): Promise<HttpResponse<T, E>> => {
    const secureParams =
      ((typeof secure === "boolean" ? secure : this.baseApiParams.secure) &&
        this.securityWorker &&
        (await this.securityWorker(this.securityData))) ||
      {};
    const requestParams = this.mergeRequestParams(params, secureParams);
    const queryString = query && this.toQueryString(query);
    const payloadFormatter = this.contentFormatters[type || ContentType.Json];
    const responseFormat = format || requestParams.format;

    return this.customFetch(
      `${baseUrl || this.baseUrl || ""}${path}${queryString ? `?${queryString}` : ""}`,
      {
        ...requestParams,
        headers: {
          ...(requestParams.headers || {}),
          ...(type && type !== ContentType.FormData
            ? { "Content-Type": type }
            : {}),
        },
        signal:
          (cancelToken
            ? this.createAbortSignal(cancelToken)
            : requestParams.signal) || null,
        body:
          typeof body === "undefined" || body === null
            ? null
            : payloadFormatter(body),
      },
    ).then(async (response) => {
      const r = response as HttpResponse<T, E>;
      r.data = null as unknown as T;
      r.error = null as unknown as E;

      const responseToParse = responseFormat ? response.clone() : response;
      const data = !responseFormat
        ? r
        : await responseToParse[responseFormat]()
            .then((data) => {
              if (r.ok) {
                r.data = data;
              } else {
                r.error = data;
              }
              return r;
            })
            .catch((e) => {
              r.error = e;
              return r;
            });

      if (cancelToken) {
        this.abortControllers.delete(cancelToken);
      }

      if (!response.ok) throw data;
      return data;
    });
  };
}

/**
 * @title OpenAPI definition
 * @version v0
 * @baseUrl http://localhost:8085
 */
export class Api<
  SecurityDataType extends unknown,
> extends HttpClient<SecurityDataType> {
  api = {
    /**
     * No description
     *
     * @tags vehicle-controller
     * @name GetVehicleById
     * @request GET:/api/logistics/vehicles/{id}
     */
    getVehicleById: (id: number, params: RequestParams = {}) =>
      this.request<VehicleDTO, any>({
        path: `/api/logistics/vehicles/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags vehicle-controller
     * @name UpdateVehicle
     * @request PUT:/api/logistics/vehicles/{id}
     */
    updateVehicle: (id: number, data: VehicleDTO, params: RequestParams = {}) =>
      this.request<VehicleDTO, any>({
        path: `/api/logistics/vehicles/${id}`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags vehicle-controller
     * @name DeleteVehicle
     * @request DELETE:/api/logistics/vehicles/{id}
     */
    deleteVehicle: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/logistics/vehicles/${id}`,
        method: "DELETE",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-region-controller
     * @name GetRegionById
     * @request GET:/api/logistics/regions/{id}
     */
    getRegionById: (id: number, params: RequestParams = {}) =>
      this.request<LocationRegionDTO, any>({
        path: `/api/logistics/regions/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-region-controller
     * @name UpdateRegion
     * @request PUT:/api/logistics/regions/{id}
     */
    updateRegion: (
      id: number,
      data: LocationRegionDTO,
      params: RequestParams = {},
    ) =>
      this.request<LocationRegionDTO, any>({
        path: `/api/logistics/regions/${id}`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-region-controller
     * @name DeleteRegion
     * @request DELETE:/api/logistics/regions/{id}
     */
    deleteRegion: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/logistics/regions/${id}`,
        method: "DELETE",
        ...params,
      }),

    /**
     * No description
     *
     * @tags package-size-controller
     * @name GetPackageSizeById
     * @request GET:/api/logistics/package-sizes/{id}
     */
    getPackageSizeById: (id: number, params: RequestParams = {}) =>
      this.request<PackageSizeDTO, any>({
        path: `/api/logistics/package-sizes/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags package-size-controller
     * @name UpdatePackageSize
     * @request PUT:/api/logistics/package-sizes/{id}
     */
    updatePackageSize: (
      id: number,
      data: PackageSizeDTO,
      params: RequestParams = {},
    ) =>
      this.request<PackageSizeDTO, any>({
        path: `/api/logistics/package-sizes/${id}`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags package-size-controller
     * @name DeletePackageSize
     * @request DELETE:/api/logistics/package-sizes/{id}
     */
    deletePackageSize: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/logistics/package-sizes/${id}`,
        method: "DELETE",
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-controller
     * @name GetDepoById
     * @request GET:/api/logistics/depos/{id}
     */
    getDepoById: (id: number, params: RequestParams = {}) =>
      this.request<DepoDTO, any>({
        path: `/api/logistics/depos/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-controller
     * @name UpdateDepo
     * @request PUT:/api/logistics/depos/{id}
     */
    updateDepo: (id: number, data: DepoDTO, params: RequestParams = {}) =>
      this.request<DepoDTO, any>({
        path: `/api/logistics/depos/${id}`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-controller
     * @name DeleteDepo
     * @request DELETE:/api/logistics/depos/{id}
     */
    deleteDepo: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/logistics/depos/${id}`,
        method: "DELETE",
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-transit-controller
     * @name GetDepoTransitById
     * @request GET:/api/logistics/depo-transits/{id}
     */
    getDepoTransitById: (id: number, params: RequestParams = {}) =>
      this.request<DepoTransitDTO, any>({
        path: `/api/logistics/depo-transits/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-transit-controller
     * @name UpdateDepoTransit
     * @request PUT:/api/logistics/depo-transits/{id}
     */
    updateDepoTransit: (
      id: number,
      data: DepoTransitDTO,
      params: RequestParams = {},
    ) =>
      this.request<DepoTransitDTO, any>({
        path: `/api/logistics/depo-transits/${id}`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-transit-controller
     * @name DeleteDepoTransit
     * @request DELETE:/api/logistics/depo-transits/{id}
     */
    deleteDepoTransit: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/logistics/depo-transits/${id}`,
        method: "DELETE",
        ...params,
      }),

    /**
     * No description
     *
     * @tags courier-controller
     * @name GetCourierById
     * @request GET:/api/logistics/couriers/{id}
     */
    getCourierById: (id: number, params: RequestParams = {}) =>
      this.request<CourierDTO, any>({
        path: `/api/logistics/couriers/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags courier-controller
     * @name UpdateCourier
     * @request PUT:/api/logistics/couriers/{id}
     */
    updateCourier: (id: number, data: CourierDTO, params: RequestParams = {}) =>
      this.request<CourierDTO, any>({
        path: `/api/logistics/couriers/${id}`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags courier-controller
     * @name DeleteCourier
     * @request DELETE:/api/logistics/couriers/{id}
     */
    deleteCourier: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/logistics/couriers/${id}`,
        method: "DELETE",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-country-controller
     * @name GetCountryById
     * @request GET:/api/logistics/countries/{id}
     */
    getCountryById: (id: number, params: RequestParams = {}) =>
      this.request<LocationCountryDTO, any>({
        path: `/api/logistics/countries/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-country-controller
     * @name UpdateCountry
     * @request PUT:/api/logistics/countries/{id}
     */
    updateCountry: (
      id: number,
      data: LocationCountryDTO,
      params: RequestParams = {},
    ) =>
      this.request<LocationCountryDTO, any>({
        path: `/api/logistics/countries/${id}`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-country-controller
     * @name DeleteCountry
     * @request DELETE:/api/logistics/countries/{id}
     */
    deleteCountry: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/logistics/countries/${id}`,
        method: "DELETE",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-city-controller
     * @name GetCityById
     * @request GET:/api/logistics/cities/{id}
     */
    getCityById: (id: number, params: RequestParams = {}) =>
      this.request<LocationCityDTO, any>({
        path: `/api/logistics/cities/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-city-controller
     * @name UpdateCity
     * @request PUT:/api/logistics/cities/{id}
     */
    updateCity: (
      id: number,
      data: LocationCityDTO,
      params: RequestParams = {},
    ) =>
      this.request<LocationCityDTO, any>({
        path: `/api/logistics/cities/${id}`,
        method: "PUT",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-city-controller
     * @name DeleteCity
     * @request DELETE:/api/logistics/cities/{id}
     */
    deleteCity: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/logistics/cities/${id}`,
        method: "DELETE",
        ...params,
      }),

    /**
     * No description
     *
     * @tags vehicle-controller
     * @name GetAllVehicles
     * @request GET:/api/logistics/vehicles
     */
    getAllVehicles: (params: RequestParams = {}) =>
      this.request<VehicleDTO[], any>({
        path: `/api/logistics/vehicles`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags vehicle-controller
     * @name CreateVehicle
     * @request POST:/api/logistics/vehicles
     */
    createVehicle: (data: VehicleDTO, params: RequestParams = {}) =>
      this.request<VehicleDTO, any>({
        path: `/api/logistics/vehicles`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-region-controller
     * @name GetAllRegions
     * @request GET:/api/logistics/regions
     */
    getAllRegions: (params: RequestParams = {}) =>
      this.request<LocationRegionDTO[], any>({
        path: `/api/logistics/regions`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-region-controller
     * @name CreateRegion
     * @request POST:/api/logistics/regions
     */
    createRegion: (data: LocationRegionDTO, params: RequestParams = {}) =>
      this.request<LocationRegionDTO, any>({
        path: `/api/logistics/regions`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags shipment-planning-controller
     * @name PlanShipmentsForDepo
     * @request POST:/api/logistics/planning/depo/{depoId}/assign-shipments
     */
    planShipmentsForDepo: (depoId: number, params: RequestParams = {}) =>
      this.request<Record<string, any>, any>({
        path: `/api/logistics/planning/depo/${depoId}/assign-shipments`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags shipment-planning-controller
     * @name PlanCrossDepoShipmentsForDepo
     * @request POST:/api/logistics/planning/depo/{depoId}/assign-cross-depo-shipments
     */
    planCrossDepoShipmentsForDepo: (
      depoId: number,
      params: RequestParams = {},
    ) =>
      this.request<Record<string, any>, any>({
        path: `/api/logistics/planning/depo/${depoId}/assign-cross-depo-shipments`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags package-size-controller
     * @name GetAllPackageSizes
     * @request GET:/api/logistics/package-sizes
     */
    getAllPackageSizes: (params: RequestParams = {}) =>
      this.request<PackageSizeDTO[], any>({
        path: `/api/logistics/package-sizes`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags package-size-controller
     * @name CreatePackageSize
     * @request POST:/api/logistics/package-sizes
     */
    createPackageSize: (data: PackageSizeDTO, params: RequestParams = {}) =>
      this.request<PackageSizeDTO, any>({
        path: `/api/logistics/package-sizes`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendByType
     * @request POST:/api/logistics/kafka/bulk/send/{entityType}
     */
    sendByType: (entityType: string, params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send/${entityType}`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendVehicles
     * @request POST:/api/logistics/kafka/bulk/send-vehicles
     */
    sendVehicles: (params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send-vehicles`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendShipments
     * @request POST:/api/logistics/kafka/bulk/send-shipments
     */
    sendShipments: (params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send-shipments`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendShipmentRoutes
     * @request POST:/api/logistics/kafka/bulk/send-shipment-routes
     */
    sendShipmentRoutes: (params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send-shipment-routes`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendShipmentRouteCouriers
     * @request POST:/api/logistics/kafka/bulk/send-shipment-route-couriers
     */
    sendShipmentRouteCouriers: (params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send-shipment-route-couriers`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendPackageSizes
     * @request POST:/api/logistics/kafka/bulk/send-package-sizes
     */
    sendPackageSizes: (params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send-package-sizes`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendLocationRegions
     * @request POST:/api/logistics/kafka/bulk/send-location-regions
     */
    sendLocationRegions: (params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send-location-regions`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendLocationCountries
     * @request POST:/api/logistics/kafka/bulk/send-location-countries
     */
    sendLocationCountries: (params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send-location-countries`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendLocationCities
     * @request POST:/api/logistics/kafka/bulk/send-location-cities
     */
    sendLocationCities: (params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send-location-cities`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendDepos
     * @request POST:/api/logistics/kafka/bulk/send-depos
     */
    sendDepos: (params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send-depos`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendCouriers
     * @request POST:/api/logistics/kafka/bulk/send-couriers
     */
    sendCouriers: (params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send-couriers`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendCountryPrices
     * @request POST:/api/logistics/kafka/bulk/send-country-prices
     */
    sendCountryPrices: (params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send-country-prices`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags kafka-bulk-sender-controller
     * @name SendAllToKafka
     * @request POST:/api/logistics/kafka/bulk/send-all
     */
    sendAllToKafka: (params: RequestParams = {}) =>
      this.request<Record<string, string>, any>({
        path: `/api/logistics/kafka/bulk/send-all`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-controller
     * @name GetAllDepos
     * @request GET:/api/logistics/depos
     */
    getAllDepos: (params: RequestParams = {}) =>
      this.request<DepoDTO[], any>({
        path: `/api/logistics/depos`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-controller
     * @name CreateDepo
     * @request POST:/api/logistics/depos
     */
    createDepo: (data: DepoDTO, params: RequestParams = {}) =>
      this.request<DepoDTO, any>({
        path: `/api/logistics/depos`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-transit-controller
     * @name GetAllDepoTransits
     * @request GET:/api/logistics/depo-transits
     */
    getAllDepoTransits: (params: RequestParams = {}) =>
      this.request<DepoTransitDTO[], any>({
        path: `/api/logistics/depo-transits`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-transit-controller
     * @name CreateDepoTransit
     * @request POST:/api/logistics/depo-transits
     */
    createDepoTransit: (data: DepoTransitDTO, params: RequestParams = {}) =>
      this.request<DepoTransitDTO, any>({
        path: `/api/logistics/depo-transits`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags courier-controller
     * @name GetAllCouriers
     * @request GET:/api/logistics/couriers
     */
    getAllCouriers: (params: RequestParams = {}) =>
      this.request<CourierDTO[], any>({
        path: `/api/logistics/couriers`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags courier-controller
     * @name CreateCourier
     * @request POST:/api/logistics/couriers
     */
    createCourier: (data: CourierDTO, params: RequestParams = {}) =>
      this.request<CourierDTO, any>({
        path: `/api/logistics/couriers`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags country-price-controller
     * @name GetAllCountryPrices
     * @request GET:/api/logistics/country-prices
     */
    getAllCountryPrices: (params: RequestParams = {}) =>
      this.request<CountryPriceDTO[], any>({
        path: `/api/logistics/country-prices`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags country-price-controller
     * @name Recalculate
     * @request POST:/api/logistics/country-prices
     */
    recalculate: (params: RequestParams = {}) =>
      this.request<object, any>({
        path: `/api/logistics/country-prices`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-country-controller
     * @name GetAllCountries
     * @request GET:/api/logistics/countries
     */
    getAllCountries: (params: RequestParams = {}) =>
      this.request<LocationCountryDTO[], any>({
        path: `/api/logistics/countries`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-country-controller
     * @name CreateCountry
     * @request POST:/api/logistics/countries
     */
    createCountry: (data: LocationCountryDTO, params: RequestParams = {}) =>
      this.request<LocationCountryDTO, any>({
        path: `/api/logistics/countries`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-city-controller
     * @name GetAllCities
     * @request GET:/api/logistics/cities
     */
    getAllCities: (params: RequestParams = {}) =>
      this.request<LocationCityDTO[], any>({
        path: `/api/logistics/cities`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-city-controller
     * @name CreateCity
     * @request POST:/api/logistics/cities
     */
    createCity: (data: LocationCityDTO, params: RequestParams = {}) =>
      this.request<LocationCityDTO, any>({
        path: `/api/logistics/cities`,
        method: "POST",
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags shipment-route-controller
     * @name GetAllShipmentRoutes
     * @request GET:/api/logistics/shipment-routes
     */
    getAllShipmentRoutes: (params: RequestParams = {}) =>
      this.request<ShipmentRouteDTO[], any>({
        path: `/api/logistics/shipment-routes`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags shipment-route-controller
     * @name GetShipmentRouteById
     * @request GET:/api/logistics/shipment-routes/{id}
     */
    getShipmentRouteById: (id: number, params: RequestParams = {}) =>
      this.request<ShipmentRouteDTO, any>({
        path: `/api/logistics/shipment-routes/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-transit-controller
     * @name GetDepoTransitsByOriginDepoId
     * @request GET:/api/logistics/depo-transits/by-origin/{originDepoId}
     */
    getDepoTransitsByOriginDepoId: (
      originDepoId: number,
      params: RequestParams = {},
    ) =>
      this.request<DepoTransitDTO[], any>({
        path: `/api/logistics/depo-transits/by-origin/${originDepoId}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-transit-controller
     * @name GetDepoTransitsByDestinationDepoId
     * @request GET:/api/logistics/depo-transits/by-destination/{destinationDepoId}
     */
    getDepoTransitsByDestinationDepoId: (
      destinationDepoId: number,
      params: RequestParams = {},
    ) =>
      this.request<DepoTransitDTO[], any>({
        path: `/api/logistics/depo-transits/by-destination/${destinationDepoId}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags country-price-controller
     * @name GetCountryPriceById
     * @request GET:/api/logistics/country-prices/{id}
     */
    getCountryPriceById: (id: number, params: RequestParams = {}) =>
      this.request<CountryPriceDTO, any>({
        path: `/api/logistics/country-prices/${id}`,
        method: "GET",
        ...params,
      }),
  };
}
