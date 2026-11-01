import { useQuery } from '@tanstack/react-query';
import { fetchPaymentStatus, getHttpStatus } from '../api/paymentsApi.ts';
import { queryKeys } from '../../shared/queryKeys.ts';

const POLL_INTERVAL_MS = 2000;

/**
 * A fizetés állapota. A Stripe-ról visszatérve az engedélyezés webhookon érkezik, ezért amíg
 * `PENDING`, és sikeres fizetésről jöttünk vissza, rövid időközönként újrakérdezzük.
 */
export const usePaymentStatus = (
    shipmentId: number | null,
    paymentToken: string | null,
    pollWhilePending: boolean,
) => {
    return useQuery({
        queryKey: queryKeys.paymentStatus(shipmentId ?? 0),
        queryFn: ({ signal }) => fetchPaymentStatus(shipmentId as number, paymentToken, signal),
        enabled: shipmentId !== null,
        // A 404 (nem a feladóé / ismeretlen) és a 409 (még nem ért be) nem javul újrapróbálással gyorsan.
        retry: (failureCount, error) => {
            const status = getHttpStatus(error);
            return status === 409 ? failureCount < 5 : status !== 404 && failureCount < 1;
        },
        refetchInterval: (query) =>
            pollWhilePending && query.state.data?.status === 'PENDING' ? POLL_INTERVAL_MS : false,
    });
};
