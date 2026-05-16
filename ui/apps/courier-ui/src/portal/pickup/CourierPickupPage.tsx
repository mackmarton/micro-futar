import {useMemo} from "react";
import {ManifestDataTable} from './components/ManifestDataTable';
import {PortalLayout} from "@package/shared-ui/PortalLayout.tsx";
import {courierNavigationItems} from "../navigation.ts";
import {useMutation} from "@tanstack/react-query";
import {useCourierPickups} from "./hooks/useCourierPickups.ts";
import {
    pickUpAllDeliveryShipmentsForCurrentDay,
} from "./api/courierPickupApi.ts";
import {useCourierAllocations} from "../allocations/hooks/useCourierAllocations.ts";

const toPickupAllErrorMessage = (error: unknown): string => {
    if (typeof error === 'object' && error !== null && 'error' in error) {
        const responseError = (error as { error?: { message?: string } }).error?.message;
        if (responseError) {
            return responseError;
        }
    }

    if (error instanceof Error && error.message.trim().length > 0) {
        return error.message;
    }

    return 'Nem sikerült az összes csomag felvétele.';
};

const toAssignmentKey = (assignmentId: number | null | undefined, shipmentRouteId: number | null | undefined): string => {
    return `${assignmentId ?? 'missing-id'}:${shipmentRouteId ?? 'missing-route'}`;
};

export const CourierPickupPage = () => {
    const {assignments, isLoading, errorMessage, retry} = useCourierPickups();
    const {allocations, retry: retryAllocations} = useCourierAllocations();
    const deliveryAssignmentKeys = useMemo(
        () =>
            new Set(
                allocations
                    .filter((allocation) => allocation.assignmentType === 'Delivery')
                    .map((allocation) => toAssignmentKey(allocation.assignmentId, allocation.shipmentRouteId)),
            ),
        [allocations],
    );
    const waitingShipmentsCount = assignments.filter((assignment) => {
        if (assignment.pickedUpForDelivery || assignment.failed) {
            return false;
        }

        return deliveryAssignmentKeys.has(toAssignmentKey(assignment.id, assignment.shipmentRouteId));
    }).length;
    const pickupAssignmentsCount = allocations.filter((allocation) => allocation.assignmentType === 'Pickup').length;
    const deliveryAssignmentsCount = allocations.filter((allocation) => allocation.assignmentType === 'Delivery').length;

    const pickupAllMutation = useMutation({
        mutationFn: pickUpAllDeliveryShipmentsForCurrentDay,
        onSuccess: () => {
            void retry();
            void retryAllocations();
        },
    });
    const pickupAllErrorMessage = pickupAllMutation.isError ? toPickupAllErrorMessage(pickupAllMutation.error) : null;

    return (
        <PortalLayout title="Csomag felvétel" activeHref="#/portal/shipment-pickup" navigationItems={courierNavigationItems}>
            <div className="space-y-6 md:space-y-8">
                <section className="rounded-xl bg-surface-container-low p-6 md:p-8">
                    <div className="mt-4 flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
                        <div>
                            <h1 className="text-4xl font-headline font-bold leading-tight text-on-surface md:text-5xl">
                                Depó átvételi <span className="text-on-primary-container">jegyzék</span>
                            </h1>
                            <p className="mt-3 font-body text-on-surface-variant">
                                {isLoading ? 'Mai lista betöltése folyamatban...' : `${waitingShipmentsCount} csomag vár átvételre a depóban`}
                            </p>
                            <div className="mt-4 flex flex-wrap items-center gap-2">
                                <span className="inline-flex rounded-full bg-primary px-3 py-1 text-xs font-semibold uppercase tracking-wide text-on-primary">
                                    Pickup: {pickupAssignmentsCount}
                                </span>
                                <span className="inline-flex rounded-full bg-tertiary px-3 py-1 text-xs font-semibold uppercase tracking-wide text-on-tertiary">
                                    Delivery: {deliveryAssignmentsCount}
                                </span>
                            </div>
                        </div>

                        <div
                            className="flex flex-col gap-3 rounded-full bg-surface-container-lowest px-4 py-3 shadow-[0_24px_42px_rgba(11,28,48,0.05)] sm:flex-row sm:items-center sm:justify-between sm:px-5">
                            <div className="px-2">
                                <p className="mt-1 font-body text-sm text-on-surface">
                                    {isLoading ? 'Betoltes...' : `${waitingShipmentsCount} csomag vár átvételre`}
                                </p>
                            </div>
                            <button
                                type="button"
                                onClick={() => {
                                    pickupAllMutation.mutate();
                                }}
                                disabled={pickupAllMutation.isPending || waitingShipmentsCount === 0}
                                className="inline-flex items-center justify-center gap-2 rounded-lg bg-primary px-4 py-2.5 font-body font-medium text-on-primary transition-all duration-200 enabled:hover:bg-[linear-gradient(95deg,#000000_0%,#0c9488_100%)] disabled:bg-gray-700"
                            >
                                <span className="material-symbols-outlined text-base leading-none" aria-hidden="true">
                                    work
                                </span>
                                {pickupAllMutation.isPending ? 'Folyamatban...' : 'Összes csomag felvétele'}
                            </button>
                        </div>
                    </div>
                    {pickupAllErrorMessage ? (
                        <p className="mt-4 font-body text-sm text-red-600">{pickupAllErrorMessage}</p>
                    ) : null}
                </section>

                {errorMessage ? (
                    <section className="rounded-xl bg-surface-container-low p-6 md:p-7">
                        <p className="font-body text-sm text-red-600">{errorMessage}</p>
                        <button
                            type="button"
                            onClick={() => {
                                void retry();
                            }}
                            className="mt-4 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-medium text-on-primary"
                        >
                            Ujratoltes
                        </button>
                    </section>
                ) : null}

                <section className="rounded-xl bg-surface-container-low p-6 md:p-7">
                    <p className="text-xs uppercase tracking-widest text-on-surface-variant">Jármű terheltsége</p>
                    <p className="mt-3 text-5xl font-headline font-bold leading-none text-on-surface md:text-6xl">68%</p>
                    <p className="mt-2 font-body text-sm text-on-surface-variant">420 L / 600 L elérhető kapacitás</p>

                    <div className="mt-6 h-3 rounded-full bg-surface-container-high">
                        <div
                            className="h-full rounded-full bg-[linear-gradient(90deg,#6bd8cb_0%,#0c9488_100%)]"
                            style={{width: '68%'}}
                        />
                    </div>

                </section>

                <section className="space-y-4">
                    <div className="rounded-xl bg-surface-container-low p-6 md:p-7">
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Szállítási jegyzék</p>
                        <h2 className="mt-2 text-2xl font-headline font-bold text-on-surface">Depó átvételi sor</h2>
                    </div>

                    <ManifestDataTable assignments={assignments}/>
                </section>
            </div>
        </PortalLayout>
    );
};
