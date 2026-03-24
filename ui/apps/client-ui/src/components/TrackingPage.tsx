import { BottomNavBar, SideNavBar, TopNavBar } from '@package/shared-ui';
import { TrackPackagePage } from '../feature-tracking/TrackPackagePage';

const sideNavigationItems = [
    { label: 'Saját csomagjaim', href: '#/my-shipments', icon: 'package_2' },
    { label: 'Csomag feladása', href: '#/create-order', icon: 'add_circle' },
    { label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping', isActive: true },
];

export const TrackingPage = () => {
  return (
    <div className="bg-surface text-on-surface min-h-screen selection:bg-primary-fixed selection:text-on-primary-fixed">
      <SideNavBar navigationItems={sideNavigationItems} ctaHref="#/create-order" />

      <main className="lg:ml-64 min-h-screen flex flex-col pb-24 lg:pb-0">
        <TopNavBar title="Nyomonkövetés" />
        <TrackPackagePage />
      </main>

      <BottomNavBar
        items={[
            { label: 'Saját csomagjaim', href: '#/my-shipments', icon: 'home', isActive: true },
            { label: 'Csomag feladása', href: '#/create-order', icon: 'add_box' },
            { label: 'Nyomonkövetés', href: '#/tracking', icon: 'local_shipping', isActive: true },
        ]}
      />
    </div>
  );
};


