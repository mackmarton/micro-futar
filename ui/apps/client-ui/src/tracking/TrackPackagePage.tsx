import {TrackingHero} from './components';
import {TrackingDetailsSection} from './components';
import {useTracking} from './hooks/useTracking.ts';
import { useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { PortalLayout } from '@package/shared-ui';

export type TrackPackagePageProps = {
    className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

export const TrackPackagePage = ({className}: TrackPackagePageProps) => {
    const [searchParams] = useSearchParams();
    const {hasSearchStarted, isLoading, errorMessage, details, search, retry} = useTracking();
    const initialTrackingNumber = useMemo(() => searchParams.get('trackingNumber')?.trim() ?? '', [searchParams]);

    useEffect(() => {
        if (!initialTrackingNumber) {
            return;
        }

        void search(initialTrackingNumber);
    }, [initialTrackingNumber, search]);

    const handleSearch = (trackingCode: string) => {
        void search(trackingCode);
    };

    return (
        <PortalLayout title="Nyomonkövetés" activeHref="#/portal/tracking" contentClassName={cn('px-6 py-8 md:p-12', className)}>
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
        </PortalLayout>
    );
};

