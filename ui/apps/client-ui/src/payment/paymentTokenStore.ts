/**
 * A fizetési token igazolja, hogy a szállítmány a feladóé (fiók nélkül is). A böngészőben marad,
 * hogy a feladó a Stripe-ról visszatérve vagy később, ugyanebből a böngészőből is fizethessen; a
 * fizetés zárolása után már nincs rá szükség. Az URL-be sosem kerül.
 */
const keyFor = (shipmentId: number) => `micro-futar:payment-token:${shipmentId}`;

export const savePaymentToken = (shipmentId: number, token: string) => {
    try {
        window.localStorage.setItem(keyFor(shipmentId), token);
    } catch {
        // Privát mód / tiltott tároló: a fizetés ettől még elindul, csak később nem folytatható.
    }
};

export const loadPaymentToken = (shipmentId: number): string | null => {
    try {
        return window.localStorage.getItem(keyFor(shipmentId));
    } catch {
        return null;
    }
};

export const clearPaymentToken = (shipmentId: number) => {
    try {
        window.localStorage.removeItem(keyFor(shipmentId));
    } catch {
        // nincs teendő
    }
};
