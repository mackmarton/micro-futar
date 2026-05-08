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
const LogisticsRegionFormPage = lazy(() =>
  import('./portal/LogisticsRegionFormPage').then((module) => ({ default: module.LogisticsRegionFormPage })),
);
const LogisticsCountryFormPage = lazy(() =>
  import('./portal/LogisticsCountryFormPage').then((module) => ({ default: module.LogisticsCountryFormPage })),
);
const LogisticsCityFormPage = lazy(() =>
  import('./portal/LogisticsCityFormPage').then((module) => ({ default: module.LogisticsCityFormPage })),
);
const LogisticsPackageSizesPage = lazy(() =>
  import('./portal/LogisticsPackageSizesPage').then((module) => ({ default: module.LogisticsPackageSizesPage })),
);
const LogisticsPackageSizeFormPage = lazy(() =>
  import('./portal/LogisticsPackageSizeFormPage').then((module) => ({ default: module.LogisticsPackageSizeFormPage })),
);
const LogisticsVehiclesPage = lazy(() =>
  import('./portal/LogisticsVehiclesPage').then((module) => ({ default: module.LogisticsVehiclesPage })),
);
const LogisticsVehicleFormPage = lazy(() =>
  import('./portal/LogisticsVehicleFormPage').then((module) => ({ default: module.LogisticsVehicleFormPage })),
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
          <Route path="locations/regions/new" element={<LogisticsRegionFormPage />} />
          <Route path="locations/regions/:regionId/edit" element={<LogisticsRegionFormPage />} />
          <Route path="locations/countries" element={<LogisticsCountriesPage />} />
          <Route path="locations/countries/new" element={<LogisticsCountryFormPage />} />
          <Route path="locations/countries/:countryId/edit" element={<LogisticsCountryFormPage />} />
          <Route path="locations/cities" element={<LogisticsCitiesPage />} />
          <Route path="locations/cities/new" element={<LogisticsCityFormPage />} />
          <Route path="locations/cities/:cityId/edit" element={<LogisticsCityFormPage />} />
          <Route path="package-sizes" element={<LogisticsPackageSizesPage />} />
          <Route path="package-sizes/new" element={<LogisticsPackageSizeFormPage />} />
          <Route path="package-sizes/:packageSizeId/edit" element={<LogisticsPackageSizeFormPage />} />
          <Route path="vehicles" element={<LogisticsVehiclesPage />} />
          <Route path="vehicles/new" element={<LogisticsVehicleFormPage />} />
          <Route path="vehicles/:vehicleId/edit" element={<LogisticsVehicleFormPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}

export default App;
