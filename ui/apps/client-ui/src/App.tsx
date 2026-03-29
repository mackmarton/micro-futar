import { useEffect, useState } from 'react';
import { useAuth } from '@package/shared-ui';
import { CreateOrderPage } from './create-order/CreateOrderPage.tsx';
import { DashboardPage } from './my-shipments/DashboardPage.tsx';
import { LandingPage } from './landing/LandingPage.tsx';
import { TrackPackagePage } from './tracking/TrackPackagePage.tsx';

type AppRoute = '/' | '/my-shipments' | '/create-order' | '/tracking';

const DEFAULT_ROUTE: AppRoute = '/';

const getRouteFromHash = (): AppRoute => {
  const hash = window.location.hash.replace('#', '');
  const [path] = hash.split('?');

  if (!path || path === '/') return '/';
  if (path === '/tracking') return '/tracking';
  if (path === '/create-order') return '/create-order';
  if (path === '/my-shipments') return '/my-shipments';

  return '/';
};

function App() {
  const [route, setRoute] = useState<AppRoute>(getRouteFromHash);
  const { user, isLoading } = useAuth();

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

  if (route === '/my-shipments') {
    return <DashboardPage />;
  }

  if (route === '/create-order') {
    return <CreateOrderPage />;
  }

  if (route === '/tracking') {
    return <TrackPackagePage />;
  }

  if (isLoading) {
    return null;
  }

  if (user) {
    return <DashboardPage />;
  }

  return <LandingPage />;
}

export default App;