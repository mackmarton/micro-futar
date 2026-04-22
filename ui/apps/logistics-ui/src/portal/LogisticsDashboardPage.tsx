import { PortalLayout } from '@package/shared-ui';
import { logisticsNavigationItems } from './navigation';

export const LogisticsDashboardPage = () => {

  return (
    <PortalLayout
      title="Dashboard"
      activeHref="#/portal/dashboard"
      navigationItems={logisticsNavigationItems}
    >
      <section className="rounded-2xl border border-outline-variant bg-surface-container-lowest p-6 md:p-8">
        <h1 className="text-2xl md:text-3xl font-headline font-bold tracking-tight">Logisztikai portál</h1>
      </section>

    </PortalLayout>
  );
};

