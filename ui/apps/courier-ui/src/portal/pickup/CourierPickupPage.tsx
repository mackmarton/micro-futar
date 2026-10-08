import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { ManifestDataTable } from './components/ManifestDataTable';
import { PortalLayout } from '@package/shared-ui';
import { toErrorMessage } from '@package/shared-core';
import { useCourierNavigationItems } from '../navigation.ts';
import { useMutation } from '@tanstack/react-query';
import { useCourierPickups } from './hooks/useCourierPickups.ts';
import {
    pickUpAllDeliveryShipmentsForCurrentDay,
} from './api/courierPickupApi.ts';
import { useCourierAllocations } from '../allocations/hooks/useCourierAllocations.ts';

const toAssignmentKey = (assignmentId: number | null | undefined, shipmentRouteId: number | null | undefined): string => {
    return `${assignmentId ?? 'missing-id'}:${shipmentRouteId ?? 'missing-route'}`;
};

export const CourierPickupPage = () => {
    const { t } = useTranslation(['pickup', 'common']);
    const courierNavigationItems = useCourierNavigationItems();
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
    const pickupAllErrorMessage = pickupAllMutation.isError
        ? toErrorMessage(pickupAllMutation.error, t('pickup:pickupAllError'))
        : null;

    return (
        <PortalLayout title={t('common:nav.pickup')} activeHref="#/portal/shipment-pickup" navigationItems={courierNavigationItems}>
            <div className="space-y-6 md:space-y-8">
                <section className="rounded-xl bg-surface-container-low p-6 md:p-8">
                    <div className="mt-4 flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
                        <div>
                            <h1 className="text-4xl font-headline font-bold leading-tight text-on-surface md:text-5xl">
                                {t('pickup:heading.prefix')} <span className="text-on-primary-container">{t('pickup:heading.highlight')}</span>
                            </h1>
                            <p className="mt-3 font-body text-on-surface-variant">
                                {isLoading ? t('pickup:subtitle.loading') : t('pickup:subtitle.waitingAtDepot', { count: waitingShipmentsCount })}
                            </p>
                            <div className="mt-4 flex flex-wrap items-center gap-2">
                                <span className="inline-flex rounded-full bg-primary px-3 py-1 text-xs font-semibold uppercase tracking-wide text-on-primary">
                                    {t('pickup:badges.pickupCount', { count: pickupAssignmentsCount })}
                                </span>
                                <span className="inline-flex rounded-full bg-tertiary px-3 py-1 text-xs font-semibold uppercase tracking-wide text-on-tertiary">
                                    {t('pickup:badges.deliveryCount', { count: deliveryAssignmentsCount })}
                                </span>
                            </div>
                        </div>

                        <div
                            className="flex flex-col gap-3 rounded-full bg-surface-container-lowest px-4 py-3 shadow-[0_24px_42px_rgba(11,28,48,0.05)] sm:flex-row sm:items-center sm:justify-between sm:px-5">
                            <div className="px-2">
                                <p className="mt-1 font-body text-sm text-on-surface">
                                    {isLoading ? t('common:status.loading') : t('pickup:summary.waiting', { count: waitingShipmentsCount })}
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
                                {pickupAllMutation.isPending ? t('common:status.pending') : t('pickup:pickupAllButton.label')}
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
                            {t('common:actions.retry')}
                        </button>
                    </section>
                ) : null}

                <section className="rounded-xl bg-surface-container-low p-6 md:p-7">
                    <p className="text-xs uppercase tracking-widest text-on-surface-variant">{t('pickup:vehicleLoad.label')}</p>
                    <p className="mt-3 text-5xl font-headline font-bold leading-none text-on-surface md:text-6xl">68%</p>
                    <p className="mt-2 font-body text-sm text-on-surface-variant">{t('pickup:vehicleLoad.capacity')}</p>

                    <div className="mt-6 h-3 rounded-full bg-surface-container-high">
                        <div
                            className="h-full rounded-full bg-[linear-gradient(90deg,#6bd8cb_0%,#0c9488_100%)]"
                            style={{width: '68%'}}
                        />
                    </div>

                </section>

                <section className="space-y-4">
                    <div className="rounded-xl bg-surface-container-low p-6 md:p-7">
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('pickup:section.eyebrow')}</p>
                        <h2 className="mt-2 text-2xl font-headline font-bold text-on-surface">{t('pickup:section.heading')}</h2>
                    </div>

                    <ManifestDataTable assignments={assignments}/>
                </section>
            </div>
        </PortalLayout>
    );
};
