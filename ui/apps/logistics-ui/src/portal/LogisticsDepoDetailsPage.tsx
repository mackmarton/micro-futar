import {useMemo} from 'react';
import {Link, Navigate, useParams} from 'react-router-dom';
import {useQuery} from '@tanstack/react-query';
import {DataTable, PortalLayout} from '@package/shared-ui';
import type {DataTableColumn} from '@package/shared-ui';
import type {DepoTransitDTO} from '@package/shared-core/api/LogisticsApiClient';
import {
    getAllDepos,
    getAllPackageSizes,
    getDepoByIdWithLookups,
    getDepoTransitsByDestinationDepoId,
    getDepoTransitsByOriginDepoId,
} from './api/logisticsDeposApi';
import {logisticsNavigationItems} from './navigation';

const valueOrFallback = (value?: string | number | boolean) => {
    if (typeof value === 'boolean') {
        return value ? 'Igen' : 'Nem';
    }

    return value ?? value === 0 ? value : 'N/A';
};

const isValidCoordinate = (value?: number): value is number =>
    typeof value === 'number' && Number.isFinite(value);

const buildMapEmbedUrl = (latitude: number, longitude: number) => {
    const delta = 0.03;
    const left = longitude - delta;
    const right = longitude + delta;
    const top = latitude + delta;
    const bottom = latitude - delta;

    return `https://www.openstreetmap.org/export/embed.html?bbox=${left}%2C${bottom}%2C${right}%2C${top}&layer=mapnik&marker=${latitude}%2C${longitude}`;
};

const decodeTransportType = (transportType: string) => {
    if (transportType === 'ROAD') {
        return 'Földi';
    } else if (transportType === 'AIR') {
        return 'Légi';
    }
    return transportType;
};

export const LogisticsDepoDetailsPage = () => {
    const params = useParams();
    const depoId = Number(params.depoId);
    const hasValidDepoId = Number.isInteger(depoId) && depoId > 0;

    const {data, isLoading, isError, error, refetch} = useQuery({
        queryKey: ['logistics', 'depo', depoId],
        queryFn: () => getDepoByIdWithLookups(depoId),
        enabled: hasValidDepoId,
    });

    const {
        data: outgoingTransits,
        isLoading: isOutgoingLoading,
        isError: isOutgoingError,
        error: outgoingError,
        refetch: refetchOutgoing,
    } = useQuery({
        queryKey: ['logistics', 'depo', depoId, 'outgoing-transits'],
        queryFn: () => getDepoTransitsByOriginDepoId(depoId),
        enabled: hasValidDepoId,
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
        enabled: hasValidDepoId,
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
        enabled: hasValidDepoId,
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
        enabled: hasValidDepoId,
    });

    const mapEmbedUrl = useMemo(() => {
        if (!data) {
            return null;
        }

        const {latitude, longitude} = data;
        if (!isValidCoordinate(latitude) || !isValidCoordinate(longitude)) {
            return null;
        }

        return buildMapEmbedUrl(latitude, longitude);
    }, [data]);

    const packageSizeNameById = useMemo(() => {
        return new Map((packageSizes ?? []).map((packageSize) => [packageSize.id, packageSize.name ?? 'N/A']));
    }, [packageSizes]);

    const depoNameById = useMemo(() => {
        const namesById = new Map<number, string>();

        for (const depo of depos ?? []) {
            if (typeof depo.id === 'number') {
                namesById.set(depo.id, depo.name ?? 'N/A');
            }
        }

        return namesById;
    }, [depos]);

    const outgoingColumns = useMemo<DataTableColumn<DepoTransitDTO>[]>(() => [
        {
            id: 'destinationDepoId',
            header: 'Cél depo',
            mobileLabel: 'Cél depo',
            cell: (transit) => {
                if (typeof transit.destinationDepoId !== 'number') {
                    return 'N/A';
                }

                const destinationDepoId = transit.destinationDepoId;
                const destinationDepoName = depoNameById.get(destinationDepoId) ?? `#${destinationDepoId}`;

                return (
                    <Link to={`/portal/depos/${destinationDepoId}`} className="text-blue-600 underline">
                        {destinationDepoName}
                    </Link>
                );
            },
        },
        {
            id: 'packageSizeId',
            header: 'Csomagméret',
            mobileLabel: 'Csomagméret',
            cell: (transit) => {
                if (typeof transit.packageSizeId !== 'number') {
                    return 'N/A';
                }

                return packageSizeNameById.get(transit.packageSizeId) ?? `#${transit.packageSizeId}`;
            },
        },
        {
            id: 'transportType',
            header: 'Szállítás típusa',
            mobileLabel: 'Szállítás típusa',
            cell: (transit) => transit.transportType ? decodeTransportType(transit.transportType) : valueOrFallback(transit.transportType),
        },
        {
            id: 'price',
            header: 'Ár',
            mobileLabel: 'Ár',
            cell: (transit) => `${valueOrFallback(transit.price)} Ft`,
        },
        {
            id: 'edit',
            header: 'Szerkesztés',
            cell: (transit) =>
                typeof transit.id === 'number' ? (
                    <Link
                        to={`/portal/depos/${depoId}/transits/${transit.id}/edit?direction=outgoing`}
                        className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
                    >
                        Szerkeszt
                    </Link>
                ) : (
                    <span className="text-on-surface-variant">N/A</span>
                ),
        },
    ], [depoId, depoNameById, packageSizeNameById]);

    const incomingColumns = useMemo<DataTableColumn<DepoTransitDTO>[]>(() => [
        {
            id: 'originDepoId',
            header: 'Forrás depo',
            mobileLabel: 'Forrás depo',
            cell: (transit) => {
                if (typeof transit.originDepoId !== 'number') {
                    return 'N/A';
                }

                const originDepoId = transit.originDepoId;
                const originDepoName = depoNameById.get(originDepoId) ?? `#${originDepoId}`;

                return (
                    <Link to={`/portal/depos/${originDepoId}`} className="text-blue-600 underline">
                        {originDepoName}
                    </Link>
                );
            },
        },
        {
            id: 'packageSizeId',
            header: 'Csomagméret',
            mobileLabel: 'Csomagméret',
            cell: (transit) => {
                if (typeof transit.packageSizeId !== 'number') {
                    return 'N/A';
                }

                return packageSizeNameById.get(transit.packageSizeId) ?? `#${transit.packageSizeId}`;
            },
        },
        {
            id: 'transportType',
            header: 'Szállítás típusa',
            mobileLabel: 'Szállítás típusa',
            cell: (transit) => transit.transportType ? decodeTransportType(transit.transportType) : valueOrFallback(transit.transportType),
        },
        {
            id: 'price',
            header: 'Ár',
            mobileLabel: 'Ár',
            cell: (transit) => `${valueOrFallback(transit.price)} Ft`,
        },
        {
            id: 'edit',
            header: 'Szerkesztés',
            cell: (transit) =>
                typeof transit.id === 'number' ? (
                    <Link
                        to={`/portal/depos/${depoId}/transits/${transit.id}/edit?direction=incoming`}
                        className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
                    >
                        Szerkeszt
                    </Link>
                ) : (
                    <span className="text-on-surface-variant">N/A</span>
                ),
        },
    ], [depoId, depoNameById, packageSizeNameById]);

    if (!hasValidDepoId) {
        return <Navigate to="/portal/depos" replace/>;
    }

    return (
        <PortalLayout title="Depo részletek" activeHref="#/portal/depos" navigationItems={logisticsNavigationItems}>
            <div className="flex flex-wrap gap-3 justify-between">
                <Link
                    to="/portal/depos"
                    className="inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary hover:bg-on-primary-container transition-colors"
                >
                    <span className="material-symbols-outlined" aria-hidden="true">
                      arrow_back
                    </span>
                    Vissza a listához
                </Link>
                {typeof data?.id === 'number' ? (
                    <Link
                        to={`/portal/depos/${data.id}/edit`}
                        className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
                    >
                        Szerkesztés
                    </Link>
                ) : null}
            </div>

            {isLoading ? (
                <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Betöltés</p>
                    <p className="mt-2 font-body text-on-surface">Depo részletek betöltése...</p>
                </section>
            ) : null}

            {isError ? (
                <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Hiba</p>
                    <p className="mt-2 font-body text-on-surface">Nem sikerült betölteni a depo részleteket.</p>
                    <p className="mt-1 font-body text-on-surface-variant">{(error as Error)?.message ?? 'Ismeretlen hiba'}</p>
                    <button
                        type="button"
                        onClick={() => {
                            void refetch();
                        }}
                        className="mt-4 inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary hover:bg-on-primary-container transition-colors"
                    >
                        Ujrapróbálás
                    </button>
                </section>
            ) : null}

            {!isLoading && !isError && data ? (
                <div className="mt-6 grid gap-4">
                    <section className="rounded-2xl bg-surface-container-low p-6 lg:col-span-2">
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Adatok</p>
                        <div className="mt-4 grid gap-3">
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Depo ID</p>
                                <p className="mt-1 font-body text-on-surface">{valueOrFallback(data.id)}</p>
                            </div>
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Név</p>
                                <p className="mt-1 font-body text-on-surface">{valueOrFallback(data.name)}</p>
                            </div>
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Ország</p>
                                <p className="mt-1 font-body text-on-surface">{valueOrFallback(data.countryName)}</p>
                            </div>
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Város</p>
                                <p className="mt-1 font-body text-on-surface">{valueOrFallback(data.cityName)}</p>
                            </div>
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Iranyítószám</p>
                                <p className="mt-1 font-body text-on-surface">{valueOrFallback(data.zip)}</p>
                            </div>
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Cím</p>
                                <p className="mt-1 font-body text-on-surface">{valueOrFallback(data.address)}</p>
                            </div>
                        </div>
                    </section>

                    <section className="rounded-2xl bg-surface-container-low p-6 lg:col-span-2">
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Térkép</p>
                        {mapEmbedUrl ? null : (
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Terkep</p>
                                <p className="mt-1 font-body text-on-surface-variant">Nincs elegendő koordináta a
                                    megjelenítéshez.</p>
                            </div>
                        )}

                        {mapEmbedUrl ? (
                            <div className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                                <iframe
                                    title="Depo helyzete terkepen"
                                    src={mapEmbedUrl}
                                    className="h-[320px] w-full rounded-lg"
                                    loading="lazy"
                                />
                            </div>
                        ) : null}
                    </section>

                    <section className="rounded-2xl bg-surface-container-low p-6 lg:col-span-2">
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Szekció</p>
                        <h2 className="mt-2 text-2xl font-headline text-on-surface">Tranzitok</h2>
                    </section>

                    <section className="lg:col-span-2">
                        <div className="mb-3 flex justify-end">
                            <Link
                                to={`/portal/depos/${depoId}/transits/new?direction=outgoing`}
                                className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                            >
                                Kimenő tranzit hozzáadása
                            </Link>
                        </div>

                        {isOutgoingLoading || isPackageSizesLoading || isDeposLoading ? (
                            <section className="rounded-2xl bg-surface-container-low p-8">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Betöltés</p>
                                <p className="mt-2 font-body text-on-surface">Kimenő depo tranzitok betöltése...</p>
                            </section>
                        ) : null}

                        {isOutgoingError || isPackageSizesError || isDeposError ? (
                            <section className="rounded-2xl bg-surface-container-low p-8">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Hiba</p>
                                <p className="mt-2 font-body text-on-surface">Nem sikerült betölteni a kimenő depo
                                    tranzitokat.</p>
                                <p className="mt-1 font-body text-on-surface-variant">
                                    {(outgoingError as Error)?.message
                                        ?? (packageSizesError as Error)?.message
                                        ?? (deposError as Error)?.message
                                        ?? 'Ismeretlen hiba'}
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
                                    Ujrapróbálás
                                </button>
                            </section>
                        ) : null}

                        {!isOutgoingLoading && !isPackageSizesLoading && !isDeposLoading && !isOutgoingError && !isPackageSizesError && !isDeposError ? (
                            <DataTable
                                data={outgoingTransits ?? []}
                                rowKey={(transit, index) => String(transit.id ?? `${transit.destinationDepoId ?? 'out'}-${index}`)}
                                title="Kimenő depo tranzitok"
                                columns={outgoingColumns}
                                emptyMessage="Ehhez a depóhoz nem található kimenő tranzit."
                                mobileCardEyebrow="Kimenő tranzit"
                                recordCountLabel={(visible, total) => `Megjelenített rekordok: ${visible} / ${total}`}
                                renderMobileActions={(transit) =>
                                    typeof transit.id === 'number' ? (
                                        <Link
                                            to={`/portal/depos/${depoId}/transits/${transit.id}/edit?direction=outgoing`}
                                            className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-semibold text-on-surface"
                                        >
                                            Szerkeszt
                                        </Link>
                                    ) : (
                                        <span className="text-on-surface-variant">N/A</span>
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
                                Bejövő tranzit hozzáadása
                            </Link>
                        </div>

                        {isIncomingLoading || isPackageSizesLoading || isDeposLoading ? (
                            <section className="rounded-2xl bg-surface-container-low p-8">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Betöltés</p>
                                <p className="mt-2 font-body text-on-surface">Bejövő depo tranzitok betöltése...</p>
                            </section>
                        ) : null}

                        {isIncomingError || isPackageSizesError || isDeposError ? (
                            <section className="rounded-2xl bg-surface-container-low p-8">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Hiba</p>
                                <p className="mt-2 font-body text-on-surface">Nem sikerült betölteni a bejövő depo
                                    tranzitokat.</p>
                                <p className="mt-1 font-body text-on-surface-variant">
                                    {(incomingError as Error)?.message
                                        ?? (packageSizesError as Error)?.message
                                        ?? (deposError as Error)?.message
                                        ?? 'Ismeretlen hiba'}
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
                                    Ujrapróbálás
                                </button>
                            </section>
                        ) : null}

                        {!isIncomingLoading && !isPackageSizesLoading && !isDeposLoading && !isIncomingError && !isPackageSizesError && !isDeposError ? (
                            <DataTable
                                data={incomingTransits ?? []}
                                rowKey={(transit, index) => String(transit.id ?? `${transit.originDepoId ?? 'in'}-${index}`)}
                                title="Bejövő depo tranzitok"
                                columns={incomingColumns}
                                emptyMessage="Ehhez a depóhoz nem található bejövő tranzit."
                                mobileCardEyebrow="Bejövő tranzit"
                                recordCountLabel={(visible, total) => `Megjelenített rekordok: ${visible} / ${total}`}
                                renderMobileActions={(transit) =>
                                    typeof transit.id === 'number' ? (
                                        <Link
                                            to={`/portal/depos/${depoId}/transits/${transit.id}/edit?direction=incoming`}
                                            className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-semibold text-on-surface"
                                        >
                                            Szerkeszt
                                        </Link>
                                    ) : (
                                        <span className="text-on-surface-variant">N/A</span>
                                    )
                                }
                            />
                        ) : null}
                    </section>
                </div>
            ) : null}
        </PortalLayout>
    );
};
