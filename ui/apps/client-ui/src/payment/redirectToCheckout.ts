import type { AppLocale } from '../i18n/createI18nInstance.ts';
import { getHttpStatus, startCheckout } from './api/paymentsApi.ts';

const NOT_READY_RETRY_DELAYS_MS = [500, 1000, 2000, 3000, 4000];

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

/**
 * Elindítja a Stripe Checkoutot és átirányít rá. Közvetlenül a rendelés után a payments service
 * még nem feltétlenül kapta meg Kafkán a fizetési kérést (409), ezért ilyenkor rövid várakozással
 * újrapróbálja.
 */
export const redirectToCheckout = async (
    shipmentId: number,
    paymentToken: string | null,
    locale: AppLocale,
): Promise<void> => {
    for (let attempt = 0; ; attempt++) {
        try {
            const checkoutUrl = await startCheckout(shipmentId, paymentToken, locale);
            window.location.assign(checkoutUrl);
            return;
        } catch (error) {
            const delay = NOT_READY_RETRY_DELAYS_MS[attempt];
            if (getHttpStatus(error) !== 409 || delay === undefined) {
                throw error;
            }
            await sleep(delay);
        }
    }
};
