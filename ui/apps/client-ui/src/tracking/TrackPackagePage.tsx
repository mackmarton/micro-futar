import {TrackingHero} from './components';
import {TrackingDetailsSection} from './components';
import {useTracking} from './hooks/useTracking.ts';
import {BottomNavBar} from "@package/shared-ui/BottomNavBar.tsx";
import {TopNavBar} from "@package/shared-ui/TopNavBar.tsx";
import {SideNavBar} from "@package/shared-ui/SideNavBar.tsx";

export type TrackPackagePageProps = {
    className?: string;
};
const sideNavigationItems = [
    {label: 'Saját csomagjaim', href: '#/my-shipments', icon: 'package_2', onlyLoggedIn: true},
    {label: 'Csomag feladása', href: '#/create-order', icon: 'add_circle'},
    {label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping', isActive: true},
];

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const TrackPackagePage = ({className}: TrackPackagePageProps) => {
    const {hasSearchStarted, isLoading, errorMessage, details, search, retry} = useTracking();

    const handleSearch = (trackingCode: string) => {
        void search(trackingCode);
    };

    return (
        <div
            className="bg-surface text-on-surface min-h-screen selection:bg-primary-fixed selection:text-on-primary-fixed">
            <SideNavBar navigationItems={sideNavigationItems}/>

            <main className="lg:ml-64 min-h-screen flex flex-col pb-24 lg:pb-0">
                <TopNavBar title="Nyomonkövetés"/>
                <div className={cn('max-w-7xl mx-auto px-6 py-8 md:p-12', className)}>
                    <TrackingHero onSearch={handleSearch}/>

                    {hasSearchStarted && isLoading && (
                        <section className="bg-surface-container-lowest rounded-xl p-6 shadow-sm text-center text-on-surface-variant">
                            Kovetesi adatok betoltese folyamatban...
                        </section>
                    )}

                    {hasSearchStarted && !isLoading && errorMessage && (
                        <section className="bg-surface-container-lowest rounded-xl p-6 shadow-sm border border-error text-center space-y-4">
                            <p className="text-error">{errorMessage}</p>
                            <button
                                type="button"
                                onClick={() => void retry()}
                                className="px-4 py-2 rounded-lg bg-primary text-on-primary font-semibold hover:bg-on-primary-container transition-all"
                            >
                                Újrapróbálás
                            </button>
                        </section>
                    )}

                    {hasSearchStarted && !isLoading && !errorMessage && details && (
                        <TrackingDetailsSection
                            trackingNumber={details.trackingNumber}
                            statusLabel={details.statusLabel}
                            deliveryTimeValue={details.deliveryTimeValue}
                            progressSteps={details.progressSteps}
                            timelineEvents={details.timelineEvents}
                            shippingAddressPrimary={details.shippingAddressPrimary}
                        />
                    )}
                </div>
            </main>

            <BottomNavBar
                items={[
                    {label: 'Saját csomagjaim', href: '#/my-shipments', icon: 'home', onlyLoggedIn: true},
                    {label: 'Csomag feladása', href: '#/create-order', icon: 'add_box'},
                    {label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping', isActive: true},
                ]}
            />
        </div>
    );
};

