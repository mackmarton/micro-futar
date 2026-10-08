import {useMemo, useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {DataTable, PortalLayout} from '@package/shared-ui';
import type {DataTableColumn} from '@package/shared-ui';
import type {CourierDTO} from '@package/shared-core/api/LogisticsApiClient';
import {Link} from 'react-router-dom';
import {useTranslation} from 'react-i18next';
import {
    getAllDepos,
    getCourierByDepoId,
    getCrossDepoCouriers,
    getVehicleRegistrationNumberById
} from '../api/logisticsDeposApi';
import {useLogisticsNavigationItems} from '../navigation';
import {LanguageSwitcher} from '../../i18n/LanguageSwitcher';

export const LogisticsCouriersPage = () => {
    const {t} = useTranslation('courier');
    const {t: tCommon} = useTranslation('common');
    const navigationItems = useLogisticsNavigationItems();
    const notAvailable = tCommon('status.notAvailable');

    const valueOrFallback = (value?: string | number) =>
        typeof value === 'number' || (typeof value === 'string' && value.length > 0) ? value : notAvailable;

    const decodeQualifiedFor = (qualifiedFor?: CourierDTO['qualifiedFor']) => {
        if (qualifiedFor === 'ROAD') {
            return t('qualifiedFor.road');
        }

        if (qualifiedFor === 'AIR') {
            return t('qualifiedFor.air');
        }

        return notAvailable;
    };

    const decodeCourierType = (courierType?: CourierDTO['courierType']) => {
        if (courierType === 'CROSS_DEPO') {
            return t('courierType.crossDepo');
        }

        if (courierType === 'DELIVERY') {
            return t('courierType.delivery');
        }

        return notAvailable;
    };

    const [selectedDepoId, setSelectedDepoId] = useState('');

    const deposQuery = useQuery({
        queryKey: ['logistics', 'depos'],
        queryFn: getAllDepos,
    });

    const crossDepoCouriersQuery = useQuery({
        queryKey: ['logistics', 'couriers', 'cross-depo'],
        queryFn: getCrossDepoCouriers,
    });

    const hasSelectedDepo = selectedDepoId.length > 0;
    const selectedDepoNumber = Number(selectedDepoId);

    const depoCouriersQuery = useQuery({
        queryKey: ['logistics', 'couriers', 'by-depo', selectedDepoNumber],
        queryFn: () => getCourierByDepoId(selectedDepoNumber),
        enabled: hasSelectedDepo && Number.isInteger(selectedDepoNumber) && selectedDepoNumber > 0,
    });

    const vehicleIds = useMemo(() => {
        const ids = new Set<number>();

        for (const courier of depoCouriersQuery.data ?? []) {
            if (typeof courier.vehicleId === 'number') {
                ids.add(courier.vehicleId);
            }
        }

        for (const courier of crossDepoCouriersQuery.data ?? []) {
            if (typeof courier.vehicleId === 'number') {
                ids.add(courier.vehicleId);
            }
        }

        return Array.from(ids).sort((a, b) => a - b);
    }, [depoCouriersQuery.data, crossDepoCouriersQuery.data]);

    const vehicleRegistrationsQuery = useQuery({
        queryKey: ['logistics', 'vehicles', 'registrations', ...vehicleIds],
        queryFn: async () => {
            const entries = await Promise.all(
                vehicleIds.map(async (vehicleId) => {
                    const registrationNumber = await getVehicleRegistrationNumberById(vehicleId);
                    return [vehicleId, registrationNumber] as const;
                }),
            );

            return new Map<number, string | undefined>(entries);
        },
        enabled: vehicleIds.length > 0,
    });

    const depoNameById = useMemo(() => {
        const map = new Map<number, string>();

        for (const depo of deposQuery.data ?? []) {
            if (typeof depo.id === 'number') {
                // depo.name backend-ről érkező szabad szöveg, nem fordítjuk.
                map.set(depo.id, depo.name ?? `#${depo.id}`);
            }
        }

        return map;
    }, [deposQuery.data]);

    const selectedDepoName = useMemo(() => {
        if (!hasSelectedDepo || !Number.isInteger(selectedDepoNumber) || selectedDepoNumber <= 0) {
            return '';
        }

        return depoNameById.get(selectedDepoNumber) ?? `#${selectedDepoNumber}`;
    }, [depoNameById, hasSelectedDepo, selectedDepoNumber]);

    const columns = useMemo<DataTableColumn<CourierDTO>[]>(
        () => [
            {
                id: 'name',
                header: t('list.columns.name'),
                mobileLabel: t('list.columns.name'),
                cell: (courier) => valueOrFallback(courier.name),
            },
            {
                id: 'email',
                header: t('list.columns.email'),
                mobileLabel: t('list.columns.email'),
                cell: (courier) => valueOrFallback(courier.email),
            },
            {
                id: 'telephone',
                header: t('list.columns.telephone'),
                mobileLabel: t('list.columns.telephone'),
                cell: (courier) => valueOrFallback(courier.telephone),
            },
            {
                id: 'vehicle',
                header: t('list.columns.vehicle'),
                mobileLabel: t('list.columns.vehicle'),
                cell: (courier) => {
                    if (typeof courier.vehicleId !== 'number') {
                        return notAvailable;
                    }

                    if (vehicleRegistrationsQuery.isLoading || vehicleRegistrationsQuery.isFetching) {
                        return tCommon('status.loadingEllipsis');
                    }

                    return valueOrFallback(vehicleRegistrationsQuery.data?.get(courier.vehicleId));
                },
            },
            {
                id: 'qualifiedFor',
                header: t('list.columns.qualifiedFor'),
                mobileLabel: t('list.columns.qualifiedFor'),
                cell: (courier) => decodeQualifiedFor(courier.qualifiedFor),
            },
            {
                id: 'courierType',
                header: t('list.columns.courierType'),
                mobileLabel: t('list.columns.courierType'),
                cell: (courier) => decodeCourierType(courier.courierType),
            },
            {
                id: 'edit',
                header: tCommon('table.editHeader'),
                cell: (courier) =>
                    typeof courier.id === 'number' ? (
                        <Link
                            to={`/portal/couriers/${courier.id}/edit`}
                            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
                        >
                            {tCommon('buttons.edit')}
                        </Link>
                    ) : (
                        <span className="text-on-surface-variant">{notAvailable}</span>
                    )
            }
        ],
        // eslint-disable-next-line react-hooks/exhaustive-deps
        [vehicleRegistrationsQuery.data, vehicleRegistrationsQuery.isFetching, vehicleRegistrationsQuery.isLoading, t, tCommon]
    );

    return (
        <PortalLayout title={t('list.heading')} activeHref="#/portal/couriers" navigationItems={navigationItems} topBarRightSlot={<LanguageSwitcher />}>
            <section className="rounded-2xl bg-surface-container-low p-6 lg:col-span-2">
                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('list.sectionEyebrow')}</p>
                <h1 className="mt-2 text-2xl font-headline text-on-surface">{t('list.heading')}</h1>
                <p className="mt-2 font-body text-on-surface-variant">
                    {t('list.description')}
                </p>
            </section>

            <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('list.depoCouriers.eyebrow')}</p>
                <h2 className="mt-2 text-2xl font-headline text-on-surface">{t('list.depoCouriers.heading')}</h2>

                <div className="mt-4 flex justify-end">
                    <Link
                        to="/portal/couriers/new?type=DELIVERY"
                        className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                    >
                        {t('list.depoCouriers.addNew')}
                    </Link>
                </div>

                <label className="mt-4 block rounded-xl bg-surface-container-lowest p-4">
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('list.depoCouriers.depoSelectLabel')}</p>
                    <select
                        value={selectedDepoId}
                        onChange={(event) => setSelectedDepoId(event.target.value)}
                        className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                        disabled={deposQuery.isLoading || deposQuery.isError}
                    >
                        <option value="">{t('list.depoCouriers.depoSelectPlaceholder')}</option>
                        {(deposQuery.data ?? []).map((depo) => (
                            <option key={depo.id ?? depo.name} value={depo.id ?? ''}>
                                {/* depo.name backend-ről érkező szabad szöveg, nem fordítjuk. */}
                                {depo.name ?? notAvailable}
                            </option>
                        ))}
                    </select>
                </label>

                {deposQuery.isLoading ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">{t('list.depoCouriers.loadingDepos')}</p>
                    </section>
                ) : null}

                {deposQuery.isError ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">{t('list.depoCouriers.errorLoadingDepos')}</p>
                        <p className="mt-1 font-body text-on-surface-variant">
                            {(deposQuery.error as Error)?.message ?? tCommon('status.unknownError')}
                        </p>
                        <button
                            type="button"
                            onClick={() => {
                                void deposQuery.refetch();
                            }}
                            className="mt-3 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                        >
                            {tCommon('buttons.retry')}
                        </button>
                    </section>
                ) : null}

                {!deposQuery.isLoading && !deposQuery.isError && (deposQuery.data ?? []).length === 0 ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">{t('list.depoCouriers.noDeposAvailable')}</p>
                    </section>
                ) : null}

                {!deposQuery.isLoading && !deposQuery.isError && !hasSelectedDepo && (deposQuery.data ?? []).length > 0 ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">{t('list.depoCouriers.selectDepoPrompt')}</p>
                    </section>
                ) : null}

                {hasSelectedDepo && depoCouriersQuery.isLoading ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">{t('list.depoCouriers.loading')}</p>
                    </section>
                ) : null}

                {hasSelectedDepo && depoCouriersQuery.isError ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">{t('list.depoCouriers.errorHeading')}</p>
                        <p className="mt-1 font-body text-on-surface-variant">
                            {(depoCouriersQuery.error as Error)?.message ?? tCommon('status.unknownError')}
                        </p>
                        <button
                            type="button"
                            onClick={() => {
                                void depoCouriersQuery.refetch();
                            }}
                            className="mt-3 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                        >
                            {tCommon('buttons.retry')}
                        </button>
                    </section>
                ) : null}

                {hasSelectedDepo && !depoCouriersQuery.isLoading && !depoCouriersQuery.isError ? (
                    <div className="mt-4">
                        {vehicleRegistrationsQuery.isError ? (
                            <section className="mb-4 rounded-xl bg-surface-container-lowest p-4">
                                <p className="font-body text-on-surface">{t('list.depoCouriers.errorLoadingVehicles')}</p>
                                <p className="mt-1 font-body text-on-surface-variant">
                                    {(vehicleRegistrationsQuery.error as Error)?.message ?? tCommon('status.unknownError')}
                                </p>
                            </section>
                        ) : null}

                        <DataTable
                            data={depoCouriersQuery.data ?? []}
                            rowKey={(courier, index) => String(courier.id ?? `${courier.name ?? 'courier'}-${index}`)}
                            title={selectedDepoName ? t('list.depoCouriers.tableTitleWithDepo', {depoName: selectedDepoName}) : t('list.depoCouriers.tableTitle')}
                            columns={columns}
                            emptyMessage={t('list.depoCouriers.empty')}
                            mobileCardEyebrow={t('list.depoCouriers.mobileEyebrow')}
                            recordCountLabel={(visible, total) => tCommon('table.recordCount', {visible, total})}
                            renderMobileActions={(courier) =>
                                typeof courier.id === 'number' ? (
                                    <Link
                                        to={`/portal/couriers/${courier.id}/edit`}
                                        className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-semibold text-on-surface"
                                    >
                                        {tCommon('buttons.edit')}
                                    </Link>
                                ) : (
                                    <span className="text-on-surface-variant">{notAvailable}</span>
                                )
                            }
                        />
                    </div>
                ) : null}
            </section>

            <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('list.crossDepoCouriers.eyebrow')}</p>
                <h2 className="mt-2 text-2xl font-headline text-on-surface">{t('list.crossDepoCouriers.heading')}</h2>

                <div className="mt-4 flex justify-end">
                    <Link
                        to="/portal/couriers/new?type=CROSS_DEPO"
                        className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                    >
                        {t('list.crossDepoCouriers.addNew')}
                    </Link>
                </div>

                {crossDepoCouriersQuery.isLoading ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">{t('list.crossDepoCouriers.loading')}</p>
                    </section>
                ) : null}

                {crossDepoCouriersQuery.isError ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">{t('list.crossDepoCouriers.errorHeading')}</p>
                        <p className="mt-1 font-body text-on-surface-variant">
                            {(crossDepoCouriersQuery.error as Error)?.message ?? tCommon('status.unknownError')}
                        </p>
                        <button
                            type="button"
                            onClick={() => {
                                void crossDepoCouriersQuery.refetch();
                            }}
                            className="mt-3 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                        >
                            {tCommon('buttons.retry')}
                        </button>
                    </section>
                ) : null}

                {!crossDepoCouriersQuery.isLoading && !crossDepoCouriersQuery.isError ? (
                    <div className="mt-4">
                        {vehicleRegistrationsQuery.isError ? (
                            <section className="mb-4 rounded-xl bg-surface-container-lowest p-4">
                                <p className="font-body text-on-surface">{t('list.crossDepoCouriers.errorLoadingVehicles')}</p>
                                <p className="mt-1 font-body text-on-surface-variant">
                                    {(vehicleRegistrationsQuery.error as Error)?.message ?? tCommon('status.unknownError')}
                                </p>
                            </section>
                        ) : null}

                        <DataTable
                            data={crossDepoCouriersQuery.data ?? []}
                            rowKey={(courier, index) => String(courier.id ?? `${courier.name ?? 'courier'}-${index}`)}
                            title={t('list.crossDepoCouriers.tableTitle')}
                            columns={columns}
                            emptyMessage={t('list.crossDepoCouriers.empty')}
                            mobileCardEyebrow={t('list.crossDepoCouriers.mobileEyebrow')}
                            recordCountLabel={(visible, total) => tCommon('table.recordCount', {visible, total})}
                            renderMobileActions={(courier) =>
                                typeof courier.id === 'number' ? (
                                    <Link
                                        to={`/portal/couriers/${courier.id}/edit`}
                                        className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-semibold text-on-surface"
                                    >
                                        {tCommon('buttons.edit')}
                                    </Link>
                                ) : (
                                    <span className="text-on-surface-variant">{notAvailable}</span>
                                )
                            }
                        />
                    </div>
                ) : null}
            </section>
        </PortalLayout>
    );
};
