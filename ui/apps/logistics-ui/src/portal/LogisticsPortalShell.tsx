import { Suspense } from 'react';
import { Outlet } from 'react-router-dom';
import { PortalLayout } from '@package/shared-ui';
import { logisticsNavigationItems } from './navigation';

export const LogisticsPortalShell = () => {
  return (
    <PortalLayout title="Logisztika" activeHref="#/portal/dashboard" navigationItems={logisticsNavigationItems}>
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
