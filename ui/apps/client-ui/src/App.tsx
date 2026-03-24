import { useEffect, useState } from 'react';
import { CreateOrderPage } from './components/CreateOrderPage';
import { DashboardPage } from './components/DashboardPage';
import { TrackingPage } from './components/TrackingPage';

type AppRoute = '/my-shipments' | '/create-order' | '/tracking';

const DEFAULT_ROUTE: AppRoute = '/my-shipments';

const getRouteFromHash = (): AppRoute => {
  const hash = window.location.hash.replace('#', '');
  if (hash === '/tracking') return '/tracking';
  if (hash === '/create-order') return '/create-order';
  return '/my-shipments';
};

function App() {
  const [route, setRoute] = useState<AppRoute>(getRouteFromHash);

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

  if (route === '/create-order') {
    return <CreateOrderPage />;
  }

  if (route === '/tracking') {
    return <TrackingPage />;
  }

  return <DashboardPage />;
}

export default App;