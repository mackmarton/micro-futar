import { useEffect, useState } from 'react';
import { useAuth } from '@package/shared-ui';
import { CreateOrderPage } from './create-order/CreateOrderPage.tsx';
import { DashboardPage } from './my-shipments/DashboardPage.tsx';
import { LandingPage } from './landing/LandingPage.tsx';
import { TrackPackagePage } from './tracking/TrackPackagePage.tsx';

type AppRoute = '/' | '/portal/dashboard' | '/portal/create-order' | '/portal/tracking';

const DEFAULT_ROUTE: AppRoute = '/';

const getRouteFromHash = (): AppRoute => {
  const hash = window.location.hash.replace('#', '');
  const [path] = hash.split('?');

  if (!path || path === '/') return '/';
  if (path === '/portal/tracking') return '/portal/tracking';
  if (path === '/portal/create-order') return '/portal/create-order';
  if (path === '/portal/dashboard') return '/portal/dashboard';

  return '/';
};

function App() {
  const [route, setRoute] = useState<AppRoute>(getRouteFromHash);
  const { isLoading } = useAuth();

  useEffect(() => {
    if (!window.location.hash) {
      window.location.hash = DEFAULT_ROUTE;
    }

    const handleHashChange = () => {
      setRoute(getRouteFromHash());
    };

    window.addEventListener('hashchange', handleHashChange);
    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  if (route === '/portal/dashboard') {
    return <DashboardPage />;
  }

  if (route === '/portal/create-order') {
    return <CreateOrderPage />;
  }

  if (route === '/portal/tracking') {
    return <TrackPackagePage />;
  }

  if (isLoading) {
    return null;
  }

  return <LandingPage />;
}

export default App;