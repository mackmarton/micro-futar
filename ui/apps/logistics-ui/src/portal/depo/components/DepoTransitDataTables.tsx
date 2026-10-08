import {useCallback, useMemo} from 'react';
import {Link} from 'react-router-dom';
import {useQuery} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {DataTable} from '@package/shared-ui';
import type {DataTableColumn} from '@package/shared-ui';
import type {DepoTransitDTO} from '@package/shared-core/api/LogisticsApiClient';
import {
    getAllDepos,
    getAllPackageSizes,
    getDepoTransitsByDestinationDepoId,
    getDepoTransitsByOriginDepoId,
} from '../../api/logisticsDeposApi';

type DepoTransitDataTablesProps = {
    depoId: number;
};

export const DepoTransitDataTables = ({depoId}: DepoTransitDataTablesProps) => {
    const {t} = useTranslation('depo');
    const {t: tCommon} = useTranslation('common');
    const notAvailable = tCommon('status.notAvailable');

    const valueOrFallback = useCallback((value?: string | number | boolean) => {
        if (typeof value === 'boolean') {
            return value ? tCommon('status.yes') : tCommon('status.no');
        }

        return typeof value === 'number' || (typeof value === 'string' && value.length > 0) ? value : notAvailable;
    }, [tCommon, notAvailable]);

    const decodeTransportType = useCallback((transportType: string) => {
        if (transportType === 'ROAD') {
            return t('transportType.road');
        } else if (transportType === 'AIR') {
            return t('transportType.air');
        }

        return transportType;
    }, [t]);

    const {
        data: outgoingTransits,
        isLoading: isOutgoingLoading,
        isError: isOutgoingError,
        error: outgoingError,
        refetch: refetchOutgoing,
    } = useQuery({
        queryKey: ['logistics', 'depo', depoId, 'outgoing-transits'],
        queryFn: () => getDepoTransitsByOriginDepoId(depoId),
    });

    const {
        data: incomingTransits,
        isLoading: isIncomingLoading,
        isError: isIncomingError,
        error: incomingError,
        refetch: refetchIncoming,
    } = useQuery({
        queryKey: ['logistics', 'depo', depoId, 'incoming-transits'],
        queryFn: () => getDepoTransitsByDestinationDepoId(depoId),
    });

    const {
        data: packageSizes,
        isLoading: isPackageSizesLoading,
        isError: isPackageSizesError,
        error: packageSizesError,
        refetch: refetchPackageSizes,
    } = useQuery({
        queryKey: ['logistics', 'package-sizes'],
        queryFn: getAllPackageSizes,
    });

    const {
        data: depos,
        isLoading: isDeposLoading,
        isError: isDeposError,
        error: deposError,
        refetch: refetchDepos,
    } = useQuery({
        queryKey: ['logistics', 'depos'],
        queryFn: getAllDepos,
    });

    const packageSizeNameById = useMemo(() => {
        // packageSize.name backend-ről érkező szabad szöveg, nem fordítjuk.
        return new Map((packageSizes ?? []).map((packageSize) => [packageSize.id, packageSize.name ?? notAvailable]));
    }, [packageSizes, notAvailable]);

    const depoNameById = useMemo(() => {
        const namesById = new Map<number, string>();

        for (const depo of depos ?? []) {
            if (typeof depo.id === 'number') {
                // depo.name backend-ről érkező szabad szöveg, nem fordítjuk.
                namesById.set(depo.id, depo.name ?? notAvailable);
            }
        }

        return namesById;
    }, [depos, notAvailable]);

    const outgoingColumns = useMemo<DataTableColumn<DepoTransitDTO>[]>(() => [
        {
            id: 'destinationDepoId',
            header: t('transitTable.columns.destinationDepo'),
            mobileLabel: t('transitTable.columns.destinationDepo'),
            cell: (transit) => {
                if (typeof transit.destinationDepoId !== 'number') {
                    return notAvailable;
                }

                const destinationDepoId = transit.destinationDepoId;
                const destinationDepoName = depoNameById.get(destinationDepoId) ?? `#${destinationDepoId}`;

                return (
                    <Link to={`/portal/depos/${destinationDepoId}`} className="text-on-primary-container underline hover:text-primary-container">
                        {destinationDepoName}
                    </Link>
                );
            },
        },
        {
            id: 'packageSizeId',
            header: t('transitTable.columns.packageSize'),
            mobileLabel: t('transitTable.columns.packageSize'),
            cell: (transit) => {
                if (typeof transit.packageSizeId !== 'number') {
                    return notAvailable;
                }

                return packageSizeNameById.get(transit.packageSizeId) ?? `#${transit.packageSizeId}`;
            },
        },
        {
            id: 'transportType',
            header: t('transitTable.columns.transportType'),
            mobileLabel: t('transitTable.columns.transportType'),
            cell: (transit) => transit.transportType ? decodeTransportType(transit.transportType) : valueOrFallback(transit.transportType),
        },
        {
            id: 'price',
            header: t('transitTable.columns.price'),
            mobileLabel: t('transitTable.columns.price'),
            cell: (transit) => `${valueOrFallback(transit.price)} Ft`,
        },
        {
            id: 'edit',
            header: tCommon('table.editHeader'),
            cell: (transit) =>
                typeof transit.id === 'number' ? (
                    <Link
                        to={`/portal/depos/${depoId}/transits/${transit.id}/edit?direction=outgoing`}
                        className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
                    >
                        {tCommon('buttons.edit')}
                    </Link>
                ) : (
                    <span className="text-on-surface-variant">{notAvailable}</span>
                ),
        },
    ], [depoId, depoNameById, packageSizeNameById, t, tCommon, notAvailable, valueOrFallback, decodeTransportType]);

    const incomingColumns = useMemo<DataTableColumn<DepoTransitDTO>[]>(() => [
        {
            id: 'originDepoId',
            header: t('transitTable.columns.originDepo'),
            mobileLabel: t('transitTable.columns.originDepo'),
            cell: (transit) => {
                if (typeof transit.originDepoId !== 'number') {
                    return notAvailable;
                }

                const originDepoId = transit.originDepoId;
                const originDepoName = depoNameById.get(originDepoId) ?? `#${originDepoId}`;

                return (
                    <Link to={`/portal/depos/${originDepoId}`} className="text-on-primary-container underline hover:text-primary-container">
                        {originDepoName}
                    </Link>
                );
            },
        },
        {
            id: 'packageSizeId',
            header: t('transitTable.columns.packageSize'),
            mobileLabel: t('transitTable.columns.packageSize'),
            cell: (transit) => {
                if (typeof transit.packageSizeId !== 'number') {
                    return notAvailable;
                }

                return packageSizeNameById.get(transit.packageSizeId) ?? `#${transit.packageSizeId}`;
            },
        },
        {
            id: 'transportType',
            header: t('transitTable.columns.transportType'),
            mobileLabel: t('transitTable.columns.transportType'),
            cell: (transit) => transit.transportType ? decodeTransportType(transit.transportType) : valueOrFallback(transit.transportType),
        },
        {
            id: 'price',
            header: t('transitTable.columns.price'),
            mobileLabel: t('transitTable.columns.price'),
            cell: (transit) => `${valueOrFallback(transit.price)} Ft`,
        },
        {
            id: 'edit',
            header: tCommon('table.editHeader'),
            cell: (transit) =>
                typeof transit.id === 'number' ? (
                    <Link
                        to={`/portal/depos/${depoId}/transits/${transit.id}/edit?direction=incoming`}
                        className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
                    >
                        {tCommon('buttons.edit')}
                    </Link>
                ) : (
                    <span className="text-on-surface-variant">{notAvailable}</span>
                ),
        },
    ], [depoId, depoNameById, packageSizeNameById, t, tCommon, notAvailable, valueOrFallback, decodeTransportType]);

    return (
        <>
            <section className="rounded-2xl bg-surface-container-low p-6 lg:col-span-2">
                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('transitTable.sectionEyebrow')}</p>
                <h2 className="mt-2 text-2xl font-headline text-on-surface">{t('transitTable.heading')}</h2>
            </section>

            <section className="lg:col-span-2">
                <div className="mb-3 flex justify-end">
                    <Link
                        to={`/portal/depos/${depoId}/transits/new?direction=outgoing`}
                        className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                    >
                        {t('transitTable.addOutgoing')}
                    </Link>
                </div>

                {isOutgoingLoading || isPackageSizesLoading || isDeposLoading ? (
                    <section className="rounded-2xl bg-surface-container-low p-8">
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{tCommon('eyebrow.loading')}</p>
                        <p className="mt-2 font-body text-on-surface">{t('transitTable.outgoingLoading')}</p>
                    </section>
                ) : null}

                {isOutgoingError || isPackageSizesError || isDeposError ? (
                    <section className="rounded-2xl bg-surface-container-low p-8">
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{tCommon('eyebrow.error')}</p>
                        <p className="mt-2 font-body text-on-surface">{t('transitTable.outgoingErrorHeading')}</p>
                        <p className="mt-1 font-body text-on-surface-variant">
                            {(outgoingError as Error)?.message
                                ?? (packageSizesError as Error)?.message
                                ?? (deposError as Error)?.message
                                ?? tCommon('status.unknownError')}
                        </p>
                        <button
                            type="button"
                            onClick={() => {
                                void refetchOutgoing();
                                void refetchPackageSizes();
                                void refetchDepos();
                            }}
                            className="mt-4 inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary hover:bg-on-primary-container transition-colors"
                        >
                            {tCommon('buttons.retry')}
                        </button>
                    </section>
                ) : null}

                {!isOutgoingLoading && !isPackageSizesLoading && !isDeposLoading && !isOutgoingError && !isPackageSizesError && !isDeposError ? (
                    <DataTable
                        data={outgoingTransits ?? []}
                        rowKey={(transit, index) => String(transit.id ?? `${transit.destinationDepoId ?? 'out'}-${index}`)}
                        title={t('transitTable.outgoingTable.title')}
                        columns={outgoingColumns}
                        emptyMessage={t('transitTable.outgoingTable.empty')}
                        mobileCardEyebrow={t('transitTable.outgoingTable.mobileEyebrow')}
                        recordCountLabel={(visible, total) => tCommon('table.recordCount', { visible, total })}
                        renderMobileActions={(transit) =>
                            typeof transit.id === 'number' ? (
                                <Link
                                    to={`/portal/depos/${depoId}/transits/${transit.id}/edit?direction=outgoing`}
                                    className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-semibold text-on-surface"
                                >
                                    {tCommon('buttons.edit')}
                                </Link>
                            ) : (
                                <span className="text-on-surface-variant">{notAvailable}</span>
                            )
                        }
                    />
                ) : null}
            </section>

            <section className="lg:col-span-2">
                <div className="mb-3 flex justify-end">
                    <Link
                        to={`/portal/depos/${depoId}/transits/new?direction=incoming`}
                        className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                    >
                        {t('transitTable.addIncoming')}
                    </Link>
                </div>

                {isIncomingLoading || isPackageSizesLoading || isDeposLoading ? (
                    <section className="rounded-2xl bg-surface-container-low p-8">
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{tCommon('eyebrow.loading')}</p>
                        <p className="mt-2 font-body text-on-surface">{t('transitTable.incomingLoading')}</p>
                    </section>
                ) : null}

                {isIncomingError || isPackageSizesError || isDeposError ? (
                    <section className="rounded-2xl bg-surface-container-low p-8">
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{tCommon('eyebrow.error')}</p>
                        <p className="mt-2 font-body text-on-surface">{t('transitTable.incomingErrorHeading')}</p>
                        <p className="mt-1 font-body text-on-surface-variant">
                            {(incomingError as Error)?.message
                                ?? (packageSizesError as Error)?.message
                                ?? (deposError as Error)?.message
                                ?? tCommon('status.unknownError')}
                        </p>
                        <button
                            type="button"
                            onClick={() => {
                                void refetchIncoming();
                                void refetchPackageSizes();
                                void refetchDepos();
                            }}
                            className="mt-4 inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary hover:bg-on-primary-container transition-colors"
                        >
                            {tCommon('buttons.retry')}
                        </button>
                    </section>
                ) : null}

                {!isIncomingLoading && !isPackageSizesLoading && !isDeposLoading && !isIncomingError && !isPackageSizesError && !isDeposError ? (
                    <DataTable
                        data={incomingTransits ?? []}
                        rowKey={(transit, index) => String(transit.id ?? `${transit.originDepoId ?? 'in'}-${index}`)}
                        title={t('transitTable.incomingTable.title')}
                        columns={incomingColumns}
                        emptyMessage={t('transitTable.incomingTable.empty')}
                        mobileCardEyebrow={t('transitTable.incomingTable.mobileEyebrow')}
                        recordCountLabel={(visible, total) => tCommon('table.recordCount', { visible, total })}
                        renderMobileActions={(transit) =>
                            typeof transit.id === 'number' ? (
                                <Link
                                    to={`/portal/depos/${depoId}/transits/${transit.id}/edit?direction=incoming`}
                                    className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-semibold text-on-surface"
                                >
                                    {tCommon('buttons.edit')}
                                </Link>
                            ) : (
                                <span className="text-on-surface-variant">{notAvailable}</span>
                            )
                        }
                    />
                ) : null}
            </section>
        </>
    );
};
