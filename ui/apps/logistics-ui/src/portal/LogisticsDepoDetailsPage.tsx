import {useMemo} from 'react';
import {Link, Navigate, useParams} from 'react-router-dom';
import {useMutation, useQuery} from '@tanstack/react-query';
import {PortalLayout} from '@package/shared-ui';
import {
    getDepoByIdWithLookups,
    planCrossDepoShipmentsForDepo,
    planShipmentsForDepo,
} from './api/logisticsDeposApi';
import {DepoTransitDataTables} from './components/DepoTransitDataTables';
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

export const LogisticsDepoDetailsPage = () => {
    const params = useParams();
    const depoId = Number(params.depoId);
    const hasValidDepoId = Number.isInteger(depoId) && depoId > 0;

    const {data, isLoading, isError, error, refetch} = useQuery({
        queryKey: ['logistics', 'depo', depoId],
        queryFn: () => getDepoByIdWithLookups(depoId),
        enabled: hasValidDepoId,
    });

    const planShipmentsMutation = useMutation({
        mutationFn: () => planShipmentsForDepo(depoId),
    });

    const planCrossDepoShipmentsMutation = useMutation({
        mutationFn: () => planCrossDepoShipmentsForDepo(depoId),
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
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Tervezés</p>
                        <h2 className="mt-2 text-2xl font-headline text-on-surface">Küldemények kiosztása</h2>

                        <div className="mt-4 grid gap-4 md:grid-cols-2">
                            <section className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Depó szállítmányok</p>
                                <p className="mt-2 font-body text-on-surface-variant">
                                    A depóhoz tartozó szállítmányok automatikus kiosztása futárokhoz.
                                </p>

                                <button
                                    type="button"
                                    onClick={() => planShipmentsMutation.mutate()}
                                    disabled={planShipmentsMutation.isPending}
                                    className="mt-4 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container disabled:cursor-not-allowed disabled:opacity-70"
                                >
                                    {planShipmentsMutation.isPending ? 'Tervezés...' : 'Szállítmány tervezés indítása'}
                                </button>

                                {planShipmentsMutation.isSuccess ? (
                                    <p className="mt-3 font-body text-on-surface-variant">A szállítmány tervezés sikeresen lefutott.</p>
                                ) : null}

                                {planShipmentsMutation.isError ? (
                                    <p className="mt-3 font-body text-on-surface-variant">
                                        {(planShipmentsMutation.error as Error)?.message ?? 'A szállítmány tervezés nem sikerült.'}
                                    </p>
                                ) : null}
                            </section>

                            <section className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Cross-depó szállítmányok</p>
                                <p className="mt-2 font-body text-on-surface-variant">
                                    A depón áthaladó cross-depó szállítmányok automatikus kiosztása.
                                </p>

                                <button
                                    type="button"
                                    onClick={() => planCrossDepoShipmentsMutation.mutate()}
                                    disabled={planCrossDepoShipmentsMutation.isPending}
                                    className="mt-4 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container disabled:cursor-not-allowed disabled:opacity-70"
                                >
                                    {planCrossDepoShipmentsMutation.isPending
                                        ? 'Tervezés...'
                                        : 'Cross-depó tervezés indítása'}
                                </button>

                                {planCrossDepoShipmentsMutation.isSuccess ? (
                                    <p className="mt-3 font-body text-on-surface-variant">A cross-depó tervezés sikeresen lefutott.</p>
                                ) : null}

                                {planCrossDepoShipmentsMutation.isError ? (
                                    <p className="mt-3 font-body text-on-surface-variant">
                                        {(planCrossDepoShipmentsMutation.error as Error)?.message
                                            ?? 'A cross-depó tervezés nem sikerült.'}
                                    </p>
                                ) : null}
                            </section>
                        </div>
                    </section>
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

                    <DepoTransitDataTables depoId={depoId}/>
                </div>
            ) : null}
        </PortalLayout>
    );
};
