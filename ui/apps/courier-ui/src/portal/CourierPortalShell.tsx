import { Suspense } from 'react';
import { Outlet } from 'react-router-dom';
import { PortalLayout } from '@package/shared-ui';
import { courierNavigationItems } from './navigation';

export const CourierPortalShell = () => {
  return (
    <PortalLayout title="Futár" activeHref="#/portal/dashboard" navigationItems={courierNavigationItems}>
      <Suspense
        fallback={
          <section className="rounded-2xl bg-surface-container-low p-6">
            <p className="font-body text-on-surface">Oldal betöltése folyamatban...</p>
          </section>
        }
      >
        <Outlet />
      </Suspense>
    </PortalLayout>
  );
};
