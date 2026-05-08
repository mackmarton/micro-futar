import { Suspense, lazy } from 'react';
import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { useAuth } from '@package/shared-ui';
import { hasLogisticsPortalAccess } from './auth/portalAccess';

const LogisticsLandingPage = lazy(() =>
  import('./landing/LogisticsLandingPage').then((module) => ({ default: module.LogisticsLandingPage })),
);
const LogisticsDashboardPage = lazy(() =>
  import('./portal/LogisticsDashboardPage').then((module) => ({ default: module.LogisticsDashboardPage })),
);
const LogisticsDeposPage = lazy(() =>
  import('./portal/LogisticsDeposPage').then((module) => ({ default: module.LogisticsDeposPage })),
);
const LogisticsDepoDetailsPage = lazy(() =>
  import('./portal/LogisticsDepoDetailsPage').then((module) => ({ default: module.LogisticsDepoDetailsPage })),
);
const LogisticsDepoFormPage = lazy(() =>
  import('./portal/LogisticsDepoFormPage').then((module) => ({ default: module.LogisticsDepoFormPage })),
);
const LogisticsDepoTransitFormPage = lazy(() =>
  import('./portal/LogisticsDepoTransitFormPage').then((module) => ({ default: module.LogisticsDepoTransitFormPage })),
);
const LogisticsCouriersPage = lazy(() =>
  import('./portal/LogisticsCouriersPage').then((module) => ({ default: module.LogisticsCouriersPage })),
);
const LogisticsCourierFormPage = lazy(() =>
  import('./portal/LogisticsCourierFormPage').then((module) => ({ default: module.LogisticsCourierFormPage })),
);
const LogisticsRegionsPage = lazy(() =>
  import('./portal/LogisticsRegionsPage').then((module) => ({ default: module.LogisticsRegionsPage })),
);
const LogisticsCountriesPage = lazy(() =>
  import('./portal/LogisticsCountriesPage').then((module) => ({ default: module.LogisticsCountriesPage })),
);
const LogisticsCitiesPage = lazy(() =>
  import('./portal/LogisticsCitiesPage').then((module) => ({ default: module.LogisticsCitiesPage })),
);

const RequireLogisticsAccess = () => {
  const { user } = useAuth();
  const isAuthorized = hasLogisticsPortalAccess(user);

  if (!isAuthorized) {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
};

function App() {
  const { isLoading } = useAuth();

  if (isLoading) {
    return null;
  }

  return (
    <Suspense fallback={null}>
      <Routes>
        <Route path="/" element={<LogisticsLandingPage />} />
        <Route path="/portal" element={<RequireLogisticsAccess />}>
          <Route path="dashboard" element={<LogisticsDashboardPage />} />
          <Route path="depos" element={<LogisticsDeposPage />} />
          <Route path="depos/new" element={<LogisticsDepoFormPage />} />
          <Route path="depos/:depoId/edit" element={<LogisticsDepoFormPage />} />
          <Route path="depos/:depoId/transits/new" element={<LogisticsDepoTransitFormPage />} />
          <Route path="depos/:depoId/transits/:depoTransitId/edit" element={<LogisticsDepoTransitFormPage />} />
          <Route path="depos/:depoId" element={<LogisticsDepoDetailsPage />} />
          <Route path="couriers" element={<LogisticsCouriersPage />} />
          <Route path="couriers/new" element={<LogisticsCourierFormPage />} />
          <Route path="couriers/:courierId/edit" element={<LogisticsCourierFormPage />} />
          <Route path="locations" element={<Navigate to="/portal/locations/regions" replace />} />
          <Route path="locations/regions" element={<LogisticsRegionsPage />} />
          <Route path="locations/countries" element={<LogisticsCountriesPage />} />
          <Route path="locations/cities" element={<LogisticsCitiesPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}

export default App;
