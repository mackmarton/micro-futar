import { PortalLayout } from '@package/shared-ui';
import {ShipmentStats} from './components/ShipmentStats.tsx';
import {ShipmentTable} from "./components/ShipmentTable.tsx";
import {useUserShipments} from './hooks/useUserShipments.ts';

export const DashboardPage = () => {
    const {shipments, stats, isLoading, errorMessage, retry} = useUserShipments();

    return (
        <PortalLayout title="Saját csomagjaim" activeHref="#/portal/dashboard" contentClassName="flex-grow">
                    <ShipmentStats stats={stats}/>

                    <div className="flex justify-between items-end mb-8 gap-6">
                        <div>
                            <h3 className="font-headline text-2xl font-bold text-on-surface">Küldemények listája</h3>
                            <p className="text-on-surface-variant">
                                Kezelje és kövesse nyomon feladott csomagjait egy helyen.
                            </p>
                        </div>

                        <a
                            href="#/portal/create-order"
                            className="hidden md:flex bg-primary text-on-primary px-6 py-3 rounded-lg font-bold items-center gap-2 hover:bg-on-primary-container transition-all"
                        >
              <span className="material-symbols-outlined" aria-hidden="true">
                local_shipping
              </span>
                            Új csomag feladása
                        </a>
                    </div>

                    {isLoading ? (
                        <section className="bg-surface-container-lowest rounded-2xl border border-outline-variant p-8 text-center text-on-surface-variant">
                            Küldemények betöltése...
                        </section>
                    ) : null}

                    {!isLoading && errorMessage ? (
                        <section className="bg-error-container/30 rounded-2xl border border-error/30 p-8 text-center">
                            <p className="text-error font-medium mb-4">{errorMessage}</p>
                            <button
                                type="button"
                                onClick={() => {
                                    void retry();
                                }}
                                className="inline-flex items-center gap-2 bg-primary text-on-primary px-4 py-2 rounded-lg font-medium hover:bg-on-primary-container transition-all"
                            >
                                <span className="material-symbols-outlined" aria-hidden="true">
                                    refresh
                                </span>
                                Újrapróbálás
                            </button>
                        </section>
                    ) : null}

                    {!isLoading && !errorMessage && shipments.length === 0 ? (
                        <section className="bg-surface-container-lowest rounded-2xl border border-outline-variant p-8 text-center text-on-surface-variant">
                            Még nincs feladott küldeményed.
                        </section>
                    ) : null}

                    {!isLoading && !errorMessage && shipments.length > 0 ? (
                        <ShipmentTable shipments={shipments}/>
                    ) : null}
        </PortalLayout>
    );
};

