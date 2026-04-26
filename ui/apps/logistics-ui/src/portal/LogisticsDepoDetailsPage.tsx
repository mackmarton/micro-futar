import {useMemo} from 'react';
import {Link, Navigate, useParams} from 'react-router-dom';
import {useQuery} from '@tanstack/react-query';
import {PortalLayout} from '@package/shared-ui';
import {getDepoByIdWithLookups} from './api/logisticsDeposApi';
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
