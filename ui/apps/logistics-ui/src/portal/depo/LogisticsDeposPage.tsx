import {useQuery} from '@tanstack/react-query';
import {PortalLayout} from '@package/shared-ui';
import {Link} from 'react-router-dom';
import {getAllDeposWithLookups} from '../api/logisticsDeposApi';
import {logisticsNavigationItems} from '../navigation';
import {DeposDataTable} from './components/DeposDataTable';

export const LogisticsDeposPage = () => {
    const {data, isLoading, isError, error, refetch} = useQuery({
        queryKey: ['logistics', 'depos'],
        queryFn: getAllDeposWithLookups,
    });

    const depos = data ?? [];

    return (
        <PortalLayout title="Depók" activeHref="#/portal/depos" navigationItems={logisticsNavigationItems}>
            <section className="rounded-2xl bg-surface-container-low p-6">
                <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Logisztika</p>
                <h1 className="mt-2 text-2xl font-headline text-on-surface">Depok</h1>
                <div className="mt-4">
                    <Link
                        to="/portal/depos/new"
                        className="inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary transition-colors hover:bg-on-primary-container"
                    >
                        Új depó létrehozása
                    </Link>
                </div>
            </section>

            {isLoading ? (
                <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Betoltes</p>
                    <p className="mt-2 font-body text-on-surface">Depok betoltese folyamatban...</p>
                </section>
            ) : null}

            {isError ? (
                <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Hiba</p>
                    <p className="mt-2 font-body text-on-surface">Nem sikerult betolteni a depokat.</p>
                    <p className="mt-1 font-body text-on-surface-variant">{(error as Error)?.message ?? 'Ismeretlen hiba'}</p>
                    <button
                        type="button"
                        onClick={() => {
                            void refetch();
                        }}
                        className="mt-4 inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 font-body font-semibold text-on-primary hover:bg-on-primary-container transition-colors"
                    >
                        Ujraprobalas
                    </button>
                </section>
            ) : null}

            {!isLoading && !isError && depos.length === 0 ? (
                <section className="mt-6 rounded-2xl bg-surface-container-low p-8">
                    <p className="text-[10px] uppercase tracking-widest text-on-surface-variant">Ures allapot</p>
                    <p className="mt-2 font-body text-on-surface">Jelenleg nincs elerheto depo rekord.</p>
                </section>
            ) : null}

            {!isLoading && !isError && depos.length > 0 ?
                <div className="mt-6">
                    <DeposDataTable depos={depos}/>
                </div>
                : null}
        </PortalLayout>
    );
};
