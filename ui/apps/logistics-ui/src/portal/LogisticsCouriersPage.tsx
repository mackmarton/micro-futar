import {useMemo, useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {DataTable, PortalLayout} from '@package/shared-ui';
import type {DataTableColumn} from '@package/shared-ui';
import type {CourierDTO} from '@package/shared-core/api/LogisticsApiClient';
import {Link} from 'react-router-dom';
import {
    getAllDepos,
    getCourierByDepoId,
    getCrossDepoCouriers,
    getVehicleRegistrationNumberById
} from './api/logisticsDeposApi';
import {logisticsNavigationItems} from './navigation';

const valueOrFallback = (value?: string | number) =>
    value === 0 || (typeof value === 'string' && value.length > 0) ? value : 'N/A';

const decodeQualifiedFor = (qualifiedFor?: CourierDTO['qualifiedFor']) => {
    if (qualifiedFor === 'ROAD') {
        return 'Földi';
    }

    if (qualifiedFor === 'AIR') {
        return 'Légi';
    }

    return 'N/A';
};

const decodeCourierType = (courierType?: CourierDTO['courierType']) => {
    if (courierType === 'CROSS_DEPO') {
        return 'Cross-depó';
    }

    if (courierType === 'DELIVERY') {
        return 'Kiszállító';
    }

    return 'N/A';
};

export const LogisticsCouriersPage = () => {
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
                header: 'Név',
                mobileLabel: 'Név',
                cell: (courier) => valueOrFallback(courier.name),
            },
            {
                id: 'email',
                header: 'Email',
                mobileLabel: 'Email',
                cell: (courier) => valueOrFallback(courier.email),
            },
            {
                id: 'telephone',
                header: 'Telefon',
                mobileLabel: 'Telefon',
                cell: (courier) => valueOrFallback(courier.telephone),
            },
            {
                id: 'vehicle',
                header: 'Jármű',
                mobileLabel: 'Jármű',
                cell: (courier) => {
                    if (typeof courier.vehicleId !== 'number') {
                        return 'N/A';
                    }

                    if (vehicleRegistrationsQuery.isLoading || vehicleRegistrationsQuery.isFetching) {
                        return 'Betöltés...';
                    }

                    return valueOrFallback(vehicleRegistrationsQuery.data?.get(courier.vehicleId));
                },
            },
            {
                id: 'qualifiedFor',
                header: 'Képzettség',
                mobileLabel: 'Képzettség',
                cell: (courier) => decodeQualifiedFor(courier.qualifiedFor),
            },
            {
                id: 'courierType',
                header: 'Típus',
                mobileLabel: 'Típus',
                cell: (courier) => decodeCourierType(courier.courierType),
            },
            {
                id: 'edit',
                header: 'Szerkesztés',
                cell: (courier) =>
                    typeof courier.id === 'number' ? (
                        <Link
                            to={`/portal/couriers/${courier.id}/edit`}
                            className="inline-flex items-center rounded-lg bg-surface-container-lowest px-3 py-1.5 text-sm font-semibold text-on-surface transition-colors hover:bg-surface-container"
                        >
                            Szerkeszt
                        </Link>
                    ) : (
                        <span className="text-on-surface-variant">N/A</span>
                    )
            }
        ],
        [vehicleRegistrationsQuery.data, vehicleRegistrationsQuery.isFetching, vehicleRegistrationsQuery.isLoading]
    );

    return (
        <PortalLayout title="Futárok" activeHref="#/portal/couriers" navigationItems={logisticsNavigationItems}>
            <section className="rounded-2xl bg-surface-container-low p-6 lg:col-span-2">
                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Szekció</p>
                <h1 className="mt-2 text-2xl font-headline text-on-surface">Futárok</h1>
                <p className="mt-2 font-body text-on-surface-variant">
                    A depóhoz tartozó futárok listázásához előbb válassz depót. A cross-depó futárok külön
                    listában jelennek meg.
                </p>
            </section>

            <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Depó futárok</p>
                <h2 className="mt-2 text-2xl font-headline text-on-surface">Depóhoz tartozó futárok</h2>

                <div className="mt-4 flex justify-end">
                    <Link
                        to="/portal/couriers/new?type=DELIVERY"
                        className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                    >
                        Új depó futár létrehozása
                    </Link>
                </div>

                <label className="mt-4 block rounded-xl bg-surface-container-lowest p-4">
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Depó választás</p>
                    <select
                        value={selectedDepoId}
                        onChange={(event) => setSelectedDepoId(event.target.value)}
                        className="mt-2 w-full rounded-lg bg-surface px-3 py-2 font-body text-on-surface"
                        disabled={deposQuery.isLoading || deposQuery.isError}
                    >
                        <option value="">Válassz depót</option>
                        {(deposQuery.data ?? []).map((depo) => (
                            <option key={depo.id ?? depo.name} value={depo.id ?? ''}>
                                {depo.name ?? 'N/A'}
                            </option>
                        ))}
                    </select>
                </label>

                {deposQuery.isLoading ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">Depók betöltése folyamatban...</p>
                    </section>
                ) : null}

                {deposQuery.isError ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">Nem sikerült betölteni a depó listát.</p>
                        <p className="mt-1 font-body text-on-surface-variant">
                            {(deposQuery.error as Error)?.message ?? 'Ismeretlen hiba'}
                        </p>
                        <button
                            type="button"
                            onClick={() => {
                                void deposQuery.refetch();
                            }}
                            className="mt-3 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                        >
                            Ujrapróbálás
                        </button>
                    </section>
                ) : null}

                {!deposQuery.isLoading && !deposQuery.isError && (deposQuery.data ?? []).length === 0 ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">Nincs elérhető depó, ezért depó futár lista sem
                            tölthető be.</p>
                    </section>
                ) : null}

                {!deposQuery.isLoading && !deposQuery.isError && !hasSelectedDepo && (deposQuery.data ?? []).length > 0 ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">Válassz ki egy depót a futárok listázásához.</p>
                    </section>
                ) : null}

                {hasSelectedDepo && depoCouriersQuery.isLoading ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">Depó futárok betöltése folyamatban...</p>
                    </section>
                ) : null}

                {hasSelectedDepo && depoCouriersQuery.isError ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">Nem sikerült betölteni a kiválasztott depó
                            futárait.</p>
                        <p className="mt-1 font-body text-on-surface-variant">
                            {(depoCouriersQuery.error as Error)?.message ?? 'Ismeretlen hiba'}
                        </p>
                        <button
                            type="button"
                            onClick={() => {
                                void depoCouriersQuery.refetch();
                            }}
                            className="mt-3 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                        >
                            Ujrapróbálás
                        </button>
                    </section>
                ) : null}

                {hasSelectedDepo && !depoCouriersQuery.isLoading && !depoCouriersQuery.isError ? (
                    <div className="mt-4">
                        {vehicleRegistrationsQuery.isError ? (
                            <section className="mb-4 rounded-xl bg-surface-container-lowest p-4">
                                <p className="font-body text-on-surface">Nem sikerült betölteni a jármű adatokat.</p>
                                <p className="mt-1 font-body text-on-surface-variant">
                                    {(vehicleRegistrationsQuery.error as Error)?.message ?? 'Ismeretlen hiba'}
                                </p>
                            </section>
                        ) : null}

                        <DataTable
                            data={depoCouriersQuery.data ?? []}
                            rowKey={(courier, index) => String(courier.id ?? `${courier.name ?? 'courier'}-${index}`)}
                            title={`Depó futárok${selectedDepoName ? ` - ${selectedDepoName}` : ''}`}
                            columns={columns}
                            emptyMessage="Ehhez a depóhoz nem található futár rekord."
                            mobileCardEyebrow="Depó futár"
                            recordCountLabel={(visible, total) => `Megjelenített rekordok: ${visible} / ${total}`}
                            renderMobileActions={(courier) =>
                                typeof courier.id === 'number' ? (
                                    <Link
                                        to={`/portal/couriers/${courier.id}/edit`}
                                        className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-semibold text-on-surface"
                                    >
                                        Szerkeszt
                                    </Link>
                                ) : (
                                    <span className="text-on-surface-variant">N/A</span>
                                )
                            }
                        />
                    </div>
                ) : null}
            </section>

            <section className="mt-6 rounded-2xl bg-surface-container-low p-6">
                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Cross-depó futárok</p>
                <h2 className="mt-2 text-2xl font-headline text-on-surface">Cross-depó futárok</h2>

                <div className="mt-4 flex justify-end">
                    <Link
                        to="/portal/couriers/new?type=CROSS_DEPO"
                        className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                    >
                        Új cross-depó futár létrehozása
                    </Link>
                </div>

                {crossDepoCouriersQuery.isLoading ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">Cross-depó futárok betöltése folyamatban...</p>
                    </section>
                ) : null}

                {crossDepoCouriersQuery.isError ? (
                    <section className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                        <p className="font-body text-on-surface">Nem sikerült betölteni a cross-depó futárokat.</p>
                        <p className="mt-1 font-body text-on-surface-variant">
                            {(crossDepoCouriersQuery.error as Error)?.message ?? 'Ismeretlen hiba'}
                        </p>
                        <button
                            type="button"
                            onClick={() => {
                                void crossDepoCouriersQuery.refetch();
                            }}
                            className="mt-3 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                        >
                            Ujrapróbálás
                        </button>
                    </section>
                ) : null}

                {!crossDepoCouriersQuery.isLoading && !crossDepoCouriersQuery.isError ? (
                    <div className="mt-4">
                        {vehicleRegistrationsQuery.isError ? (
                            <section className="mb-4 rounded-xl bg-surface-container-lowest p-4">
                                <p className="font-body text-on-surface">Nem sikerült betölteni a jármű adatokat.</p>
                                <p className="mt-1 font-body text-on-surface-variant">
                                    {(vehicleRegistrationsQuery.error as Error)?.message ?? 'Ismeretlen hiba'}
                                </p>
                            </section>
                        ) : null}

                        <DataTable
                            data={crossDepoCouriersQuery.data ?? []}
                            rowKey={(courier, index) => String(courier.id ?? `${courier.name ?? 'courier'}-${index}`)}
                            title="Cross-depó futárok"
                            columns={columns}
                            emptyMessage="Nem található cross-depó futár rekord."
                            mobileCardEyebrow="Cross-depó futár"
                            recordCountLabel={(visible, total) => `Megjelenített rekordok: ${visible} / ${total}`}
                            renderMobileActions={(courier) =>
                                typeof courier.id === 'number' ? (
                                    <Link
                                        to={`/portal/couriers/${courier.id}/edit`}
                                        className="inline-flex items-center rounded-lg bg-surface px-3 py-1.5 text-sm font-semibold text-on-surface"
                                    >
                                        Szerkeszt
                                    </Link>
                                ) : (
                                    <span className="text-on-surface-variant">N/A</span>
                                )
                            }
                        />
                    </div>
                ) : null}
            </section>
        </PortalLayout>
    );
};
