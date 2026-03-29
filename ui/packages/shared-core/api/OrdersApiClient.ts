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

export interface ShipmentDTO {
  /** @format int64 */
  id?: number;
  senderName?: string;
  senderEmail?: string;
  senderPhone?: string;
  /** @format int64 */
  senderLocationCountryId?: number;
  senderZip?: string;
  /** @format int64 */
  senderLocationCityId?: number;
  senderAddress?: string;
  recipientName?: string;
  recipientEmail?: string;
  recipientPhone?: string;
  /** @format int64 */
  recipientLocationCountryId?: number;
  recipientZip?: string;
  /** @format int64 */
  recipientLocationCityId?: number;
  recipientAddress?: string;
  /** @format int64 */
  packageSizeId?: number;
  confirmed?: boolean;
  parcelNumber?: string;
  /** @format double */
  price?: number;
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
     * @tags shipment-controller
     * @name NewShipment
     * @request POST:/api/orders/shipments/new
     */
    newShipment: (data: ShipmentDTO, params: RequestParams = {}) =>
      this.request<ShipmentDTO, any>({
        path: `/api/orders/shipments/new`,
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
     * @request GET:/api/orders/regions
     */
    getAllRegions: (params: RequestParams = {}) =>
      this.request<LocationRegionDTO[], any>({
        path: `/api/orders/regions`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-region-controller
     * @name GetRegionById
     * @request GET:/api/orders/regions/{id}
     */
    getRegionById: (id: number, params: RequestParams = {}) =>
      this.request<LocationRegionDTO, any>({
        path: `/api/orders/regions/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags package-size-controller
     * @name GetAllPackageSizes
     * @request GET:/api/orders/package-sizes
     */
    getAllPackageSizes: (params: RequestParams = {}) =>
      this.request<PackageSizeDTO[], any>({
        path: `/api/orders/package-sizes`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags package-size-controller
     * @name GetPackageSizeById
     * @request GET:/api/orders/package-sizes/{id}
     */
    getPackageSizeById: (id: number, params: RequestParams = {}) =>
      this.request<PackageSizeDTO, any>({
        path: `/api/orders/package-sizes/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags country-price-controller
     * @name GetAllCountryPrices
     * @request GET:/api/orders/country-prices
     */
    getAllCountryPrices: (params: RequestParams = {}) =>
      this.request<CountryPriceDTO[], any>({
        path: `/api/orders/country-prices`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags country-price-controller
     * @name GetCountryPriceById
     * @request GET:/api/orders/country-prices/{id}
     */
    getCountryPriceById: (id: number, params: RequestParams = {}) =>
      this.request<CountryPriceDTO, any>({
        path: `/api/orders/country-prices/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-country-controller
     * @name GetAllCountries
     * @request GET:/api/orders/countries
     */
    getAllCountries: (params: RequestParams = {}) =>
      this.request<LocationCountryDTO[], any>({
        path: `/api/orders/countries`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-country-controller
     * @name GetCountryById
     * @request GET:/api/orders/countries/{id}
     */
    getCountryById: (id: number, params: RequestParams = {}) =>
      this.request<LocationCountryDTO, any>({
        path: `/api/orders/countries/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-city-controller
     * @name GetAllCities
     * @request GET:/api/orders/cities
     */
    getAllCities: (params: RequestParams = {}) =>
      this.request<LocationCityDTO[], any>({
        path: `/api/orders/cities`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags location-city-controller
     * @name GetCityById
     * @request GET:/api/orders/cities/{id}
     */
    getCityById: (id: number, params: RequestParams = {}) =>
      this.request<LocationCityDTO, any>({
        path: `/api/orders/cities/${id}`,
        method: "GET",
        ...params,
      }),
  };
}
