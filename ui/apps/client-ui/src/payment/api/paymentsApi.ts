import { Api, type PaymentStatusResponseDTO } from '@package/shared-core/api/PaymentsApiClient';
import { resolveApiBaseUrl } from '@package/shared-core';
import type { AppLocale } from '../../i18n/createI18nInstance.ts';

export type PaymentStatus = NonNullable<PaymentStatusResponseDTO['status']>;

const PAYMENT_TOKEN_HEADER = 'X-Payment-Token';

const paymentsApiClient = new Api({
    baseUrl: resolveApiBaseUrl(),
    baseApiParams: {
        // A bejelentkezett feladó token nélkül, a session alapján is fizethet.
        credentials: 'include',
    },
});

/** A generált kliens a hibás választ (HTTP státusszal) dobja tovább. */
export const getHttpStatus = (error: unknown): number | undefined => {
    if (typeof error === 'object' && error !== null && 'status' in error) {
        const status = (error as { status?: unknown }).status;
        return typeof status === 'number' ? status : undefined;
    }
    return undefined;
};

export const startCheckout = async (
    shipmentId: number,
    paymentToken: string | null,
    locale: AppLocale,
): Promise<string> => {
    const response = await paymentsApiClient.api.startCheckout(
        { shipmentId, paymentToken: paymentToken ?? undefined, locale },
        { format: 'json' },
    );
    const checkoutUrl = response.data.checkoutUrl;
    if (!checkoutUrl) {
        throw new Error('Missing checkout URL');
    }
    return checkoutUrl;
};

export const fetchPaymentStatus = async (
    shipmentId: number,
    paymentToken: string | null,
    signal?: AbortSignal,
): Promise<PaymentStatusResponseDTO> => {
    const response = await paymentsApiClient.api.getStatus(shipmentId, {
        signal,
        format: 'json',
        headers: paymentToken ? { [PAYMENT_TOKEN_HEADER]: paymentToken } : undefined,
    });
    return response.data;
};
