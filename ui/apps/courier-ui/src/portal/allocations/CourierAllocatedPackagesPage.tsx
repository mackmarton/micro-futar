import { PortalLayout } from '@package/shared-ui';
import { courierNavigationItems } from '../navigation.ts';
import { CourierAllocationsList } from './components/CourierAllocationsList.tsx';
import { CourierAllocationsMap } from './components/CourierAllocationsMap.tsx';
import { useCourierAllocations } from './hooks/useCourierAllocations.ts';

export const CourierAllocatedPackagesPage = () => {
  const { allocations, isLoading, errorMessage, retry } = useCourierAllocations();

  return (
    <PortalLayout title="Kiosztott csomagok" activeHref="#/portal/allocated-packages" navigationItems={courierNavigationItems}>
      <div className="space-y-6 md:space-y-8">
        <section className="rounded-xl bg-surface-container-low p-6 md:p-8">
          <h1 className="text-4xl font-headline font-bold leading-tight text-on-surface md:text-5xl">
            Mai napra kiosztott <span className="text-on-primary-container">csomagok</span>
          </h1>
          <p className="mt-3 font-body text-on-surface-variant">
            {isLoading ? 'Kiosztások betöltése folyamatban...' : `${allocations.length} darab csomag jelenik meg listában és térképen.`}
          </p>
        </section>

        {errorMessage ? (
          <section className="rounded-xl bg-surface-container-low p-6 md:p-7">
            <p className="font-body text-sm text-red-600">{errorMessage}</p>
            <button
              type="button"
              onClick={() => {
                void retry();
              }}
              className="mt-4 inline-flex items-center rounded-lg bg-primary px-4 py-2 font-body font-medium text-on-primary"
            >
              Ujratoltes
            </button>
          </section>
        ) : null}

        <section className="space-y-4">
          <CourierAllocationsMap allocations={allocations} />
          <CourierAllocationsList allocations={allocations} isLoading={isLoading} />
        </section>
      </div>
    </PortalLayout>
  );
};
