import {useMemo} from 'react';
import {Link, Navigate, useParams} from 'react-router-dom';
import {useMutation, useQuery} from '@tanstack/react-query';
import {PortalLayout} from '@package/shared-ui';
import {useTranslation} from 'react-i18next';
import {
    getDepoByIdWithLookups,
    planCrossDepoShipmentsForDepo,
    planShipmentsForDepo,
} from '../api/logisticsDeposApi';
import {DepoTransitDataTables} from './components/DepoTransitDataTables';
import {useLogisticsNavigationItems} from '../navigation';
import {LanguageSwitcher} from '../../i18n/LanguageSwitcher';

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
    const {t} = useTranslation('depo');
    const {t: tCommon} = useTranslation('common');
    const navigationItems = useLogisticsNavigationItems();

    const valueOrFallback = (value?: string | number | boolean) => {
        if (typeof value === 'boolean') {
            return value ? tCommon('status.yes') : tCommon('status.no');
        }

        return typeof value === 'number' || (typeof value === 'string' && value.length > 0) ? value : tCommon('status.notAvailable');
    };

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
        <PortalLayout
            title={t('details.title')}
            activeHref="#/portal/depos"
            navigationItems={navigationItems}
            topBarRightSlot={<LanguageSwitcher />}
            brandSubtitle={tCommon('brand.subtitle')}
        >
            <div className="flex flex-wrap gap-3 justify-between">
                <Link
                    to="/portal/depos"
                    className="inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary hover:bg-on-primary-container transition-colors"
                >
                    <span className="material-symbols-outlined" aria-hidden="true">
                      arrow_back
                    </span>
                    {t('details.backToList')}
                </Link>
                {typeof data?.id === 'number' ? (
                    <Link
                        to={`/portal/depos/${data.id}/edit`}
                        className="inline-flex items-center rounded-lg bg-surface-container-lowest px-4 py-2 font-body font-semibold text-on-surface transition-colors hover:bg-surface-container"
                    >
                        {t('details.editButton')}
                    </Link>
                ) : null}
            </div>

            {isLoading ? (
                <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{tCommon('eyebrow.loading')}</p>
                    <p className="mt-2 font-body text-on-surface">{t('details.loading')}</p>
                </section>
            ) : null}

            {isError ? (
                <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{tCommon('eyebrow.error')}</p>
                    <p className="mt-2 font-body text-on-surface">{t('details.errorHeading')}</p>
                    <p className="mt-1 font-body text-on-surface-variant">{(error as Error)?.message ?? tCommon('status.unknownError')}</p>
                    <button
                        type="button"
                        onClick={() => {
                            void refetch();
                        }}
                        className="mt-4 inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary hover:bg-on-primary-container transition-colors"
                    >
                        {tCommon('buttons.retry')}
                    </button>
                </section>
            ) : null}

            {!isLoading && !isError && data ? (
                <div className="mt-6 grid gap-4">

                    <section className="rounded-2xl bg-surface-container-low p-6 lg:col-span-2">
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('details.planning.eyebrow')}</p>
                        <h2 className="mt-2 text-2xl font-headline text-on-surface">{t('details.planning.heading')}</h2>

                        <div className="mt-4 grid gap-4 md:grid-cols-2">
                            <section className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('details.planning.depoShipments.eyebrow')}</p>
                                <p className="mt-2 font-body text-on-surface-variant">
                                    {t('details.planning.depoShipments.description')}
                                </p>

                                <button
                                    type="button"
                                    onClick={() => planShipmentsMutation.mutate()}
                                    disabled={planShipmentsMutation.isPending}
                                    className="mt-4 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container disabled:cursor-not-allowed disabled:opacity-70"
                                >
                                    {planShipmentsMutation.isPending ? t('details.planning.depoShipments.pendingButton') : t('details.planning.depoShipments.startButton')}
                                </button>

                                {planShipmentsMutation.isSuccess ? (
                                    <p className="mt-3 font-body text-on-surface-variant">{t('details.planning.depoShipments.success')}</p>
                                ) : null}

                                {planShipmentsMutation.isError ? (
                                    <p className="mt-3 font-body text-on-surface-variant">
                                        {(planShipmentsMutation.error as Error)?.message ?? t('details.planning.depoShipments.errorFallback')}
                                    </p>
                                ) : null}
                            </section>

                            <section className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('details.planning.crossDepoShipments.eyebrow')}</p>
                                <p className="mt-2 font-body text-on-surface-variant">
                                    {t('details.planning.crossDepoShipments.description')}
                                </p>

                                <button
                                    type="button"
                                    onClick={() => planCrossDepoShipmentsMutation.mutate()}
                                    disabled={planCrossDepoShipmentsMutation.isPending}
                                    className="mt-4 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container disabled:cursor-not-allowed disabled:opacity-70"
                                >
                                    {planCrossDepoShipmentsMutation.isPending
                                        ? t('details.planning.crossDepoShipments.pendingButton')
                                        : t('details.planning.crossDepoShipments.startButton')}
                                </button>

                                {planCrossDepoShipmentsMutation.isSuccess ? (
                                    <p className="mt-3 font-body text-on-surface-variant">{t('details.planning.crossDepoShipments.success')}</p>
                                ) : null}

                                {planCrossDepoShipmentsMutation.isError ? (
                                    <p className="mt-3 font-body text-on-surface-variant">
                                        {(planCrossDepoShipmentsMutation.error as Error)?.message
                                            ?? t('details.planning.crossDepoShipments.errorFallback')}
                                    </p>
                                ) : null}
                            </section>
                        </div>
                    </section>
                    <section className="rounded-2xl bg-surface-container-low p-6 lg:col-span-2">
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('details.data.eyebrow')}</p>
                        <div className="mt-4 grid gap-3">
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('details.data.fields.id')}</p>
                                <p className="mt-1 font-body text-on-surface">{valueOrFallback(data.id)}</p>
                            </div>
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('details.data.fields.name')}</p>
                                {/* data.name backend-ről érkező szabad szöveg, nem fordítjuk. */}
                                <p className="mt-1 font-body text-on-surface">{valueOrFallback(data.name)}</p>
                            </div>
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('details.data.fields.country')}</p>
                                {/* data.countryName backend-ről érkező szabad szöveg, nem fordítjuk. */}
                                <p className="mt-1 font-body text-on-surface">{valueOrFallback(data.countryName)}</p>
                            </div>
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('details.data.fields.city')}</p>
                                {/* data.cityName backend-ről érkező szabad szöveg, nem fordítjuk. */}
                                <p className="mt-1 font-body text-on-surface">{valueOrFallback(data.cityName)}</p>
                            </div>
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('details.data.fields.zip')}</p>
                                <p className="mt-1 font-body text-on-surface">{valueOrFallback(data.zip)}</p>
                            </div>
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('details.data.fields.address')}</p>
                                <p className="mt-1 font-body text-on-surface">{valueOrFallback(data.address)}</p>
                            </div>
                        </div>
                    </section>

                    <section className="rounded-2xl bg-surface-container-low p-6 lg:col-span-2">
                        <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('details.map.eyebrow')}</p>
                        {mapEmbedUrl ? null : (
                            <div className="rounded-xl bg-surface-container-lowest p-4">
                                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">{t('details.map.eyebrow')}</p>
                                <p className="mt-1 font-body text-on-surface-variant">{t('details.map.noCoordinates')}</p>
                            </div>
                        )}

                        {mapEmbedUrl ? (
                            <div className="mt-4 rounded-xl bg-surface-container-lowest p-4">
                                <iframe
                                    title={t('details.map.iframeTitle')}
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
