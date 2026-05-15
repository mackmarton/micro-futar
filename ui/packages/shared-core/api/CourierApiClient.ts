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
  /** @format double */
  senderLatitude?: number;
  /** @format double */
  senderLongitude?: number;
  recipientName?: string;
  recipientEmail?: string;
  recipientPhone?: string;
  /** @format int64 */
  recipientLocationCountryId?: number;
  recipientZip?: string;
  /** @format int64 */
  recipientLocationCityId?: number;
  recipientAddress?: string;
  /** @format double */
  recipientLatitude?: number;
  /** @format double */
  recipientLongitude?: number;
  /** @format int64 */
  packageSizeId?: number;
  confirmed?: boolean;
  delivered?: boolean;
  parcelNumber?: string;
  /** @format double */
  price?: number;
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

export interface ShipmentRouteCourierDTO {
  /** @format int64 */
  id?: number;
  /** @format int64 */
  courierId?: number;
  /** @format int64 */
  shipmentRouteId?: number;
  /** @format date */
  dateAssignedFor?: string;
  pickedUpForDelivery?: boolean;
  failed?: boolean;
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
     * @tags shipment-route-courier-controller
     * @name FulfillShipmentRouteAssignment
     * @request POST:/api/courier/shipment-route-couriers/{id}/fulfill
     */
    fulfillShipmentRouteAssignment: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/courier/shipment-route-couriers/${id}/fulfill`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags shipment-route-courier-controller
     * @name FailShipmentRouteAssignment
     * @request POST:/api/courier/shipment-route-couriers/{id}/fail
     */
    failShipmentRouteAssignment: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/courier/shipment-route-couriers/${id}/fail`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags shipment-route-courier-controller
     * @name PickUpAllShipmentsForCurrentDay
     * @request POST:/api/courier/shipment-route-couriers/pickup-all-for-today
     */
    pickUpAllShipmentsForCurrentDay: (params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/courier/shipment-route-couriers/pickup-all-for-today`,
        method: "POST",
        ...params,
      }),

    /**
     * No description
     *
     * @tags vehicle-controller
     * @name FindAll
     * @request GET:/api/courier/vehicles
     */
    findAll: (params: RequestParams = {}) =>
      this.request<VehicleDTO[], any>({
        path: `/api/courier/vehicles`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags vehicle-controller
     * @name FindById
     * @request GET:/api/courier/vehicles/{id}
     */
    findById: (id: number, params: RequestParams = {}) =>
      this.request<VehicleDTO, any>({
        path: `/api/courier/vehicles/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags shipment-controller
     * @name FindAll1
     * @request GET:/api/courier/shipments
     */
    findAll1: (params: RequestParams = {}) =>
      this.request<ShipmentDTO[], any>({
        path: `/api/courier/shipments`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags shipment-controller
     * @name FindById1
     * @request GET:/api/courier/shipments/{id}
     */
    findById1: (id: number, params: RequestParams = {}) =>
      this.request<ShipmentDTO, any>({
        path: `/api/courier/shipments/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags shipment-route-controller
     * @name FindAll2
     * @request GET:/api/courier/shipment-routes
     */
    findAll2: (params: RequestParams = {}) =>
      this.request<ShipmentRouteDTO[], any>({
        path: `/api/courier/shipment-routes`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags shipment-route-controller
     * @name FindById2
     * @request GET:/api/courier/shipment-routes/{id}
     */
    findById2: (id: number, params: RequestParams = {}) =>
      this.request<ShipmentRouteDTO, any>({
        path: `/api/courier/shipment-routes/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags shipment-route-courier-controller
     * @name FindAllForCourierForCurrentDay
     * @request GET:/api/courier/shipment-route-couriers
     */
    findAllForCourierForCurrentDay: (params: RequestParams = {}) =>
      this.request<ShipmentRouteCourierDTO[], any>({
        path: `/api/courier/shipment-route-couriers`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags shipment-route-courier-controller
     * @name FindById3
     * @request GET:/api/courier/shipment-route-couriers/{id}
     */
    findById3: (id: number, params: RequestParams = {}) =>
      this.request<ShipmentRouteCourierDTO, any>({
        path: `/api/courier/shipment-route-couriers/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags package-size-controller
     * @name FindAll3
     * @request GET:/api/courier/package-sizes
     */
    findAll3: (params: RequestParams = {}) =>
      this.request<PackageSizeDTO[], any>({
        path: `/api/courier/package-sizes`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags package-size-controller
     * @name FindById4
     * @request GET:/api/courier/package-sizes/{id}
     */
    findById4: (id: number, params: RequestParams = {}) =>
      this.request<PackageSizeDTO, any>({
        path: `/api/courier/package-sizes/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-controller
     * @name FindAll4
     * @request GET:/api/courier/depos
     */
    findAll4: (params: RequestParams = {}) =>
      this.request<DepoDTO[], any>({
        path: `/api/courier/depos`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags depo-controller
     * @name FindById5
     * @request GET:/api/courier/depos/{id}
     */
    findById5: (id: number, params: RequestParams = {}) =>
      this.request<DepoDTO, any>({
        path: `/api/courier/depos/${id}`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags courier-controller
     * @name FindAll5
     * @request GET:/api/courier/couriers
     */
    findAll5: (params: RequestParams = {}) =>
      this.request<CourierDTO[], any>({
        path: `/api/courier/couriers`,
        method: "GET",
        ...params,
      }),

    /**
     * No description
     *
     * @tags courier-controller
     * @name FindById6
     * @request GET:/api/courier/couriers/{id}
     */
    findById6: (id: number, params: RequestParams = {}) =>
      this.request<CourierDTO, any>({
        path: `/api/courier/couriers/${id}`,
        method: "GET",
        ...params,
      }),
  };
}
