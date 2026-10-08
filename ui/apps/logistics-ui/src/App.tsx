import { Suspense, lazy } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { PortalShell, RequireAccess, useAuth } from '@package/shared-ui';
import { hasLogisticsPortalAccess } from './auth/portalAccess';
import { useLogisticsNavigationItems } from './portal/navigation';
import { LanguageSwitcher } from './i18n/LanguageSwitcher';

const LogisticsLandingPage = lazy(() =>
  import('./landing/LogisticsLandingPage').then((module) => ({ default: module.LogisticsLandingPage })),
);
const LogisticsDeposPage = lazy(() =>
  import('./portal/depo/LogisticsDeposPage').then((module) => ({ default: module.LogisticsDeposPage })),
);
const LogisticsDepoDetailsPage = lazy(() =>
  import('./portal/depo/LogisticsDepoDetailsPage').then((module) => ({ default: module.LogisticsDepoDetailsPage })),
);
const LogisticsDepoFormPage = lazy(() =>
  import('./portal/depo/LogisticsDepoFormPage').then((module) => ({ default: module.LogisticsDepoFormPage })),
);
const LogisticsDepoTransitFormPage = lazy(() =>
  import('./portal/depo/LogisticsDepoTransitFormPage').then((module) => ({ default: module.LogisticsDepoTransitFormPage })),
);
const LogisticsCouriersPage = lazy(() =>
  import('./portal/courier/LogisticsCouriersPage').then((module) => ({ default: module.LogisticsCouriersPage })),
);
const LogisticsCourierFormPage = lazy(() =>
  import('./portal/courier/LogisticsCourierFormPage').then((module) => ({ default: module.LogisticsCourierFormPage })),
);
const LogisticsRegionsPage = lazy(() =>
  import('./portal/location/region/LogisticsRegionsPage').then((module) => ({ default: module.LogisticsRegionsPage })),
);
const LogisticsCountriesPage = lazy(() =>
  import('./portal/location/country/LogisticsCountriesPage').then((module) => ({ default: module.LogisticsCountriesPage })),
);
const LogisticsCitiesPage = lazy(() =>
  import('./portal/location/city/LogisticsCitiesPage').then((module) => ({ default: module.LogisticsCitiesPage })),
);
const LogisticsRegionFormPage = lazy(() =>
  import('./portal/location/region/LogisticsRegionFormPage').then((module) => ({ default: module.LogisticsRegionFormPage })),
);
const LogisticsCountryFormPage = lazy(() =>
  import('./portal/location/country/LogisticsCountryFormPage').then((module) => ({ default: module.LogisticsCountryFormPage })),
);
const LogisticsCityFormPage = lazy(() =>
  import('./portal/location/city/LogisticsCityFormPage').then((module) => ({ default: module.LogisticsCityFormPage })),
);
const LogisticsPackageSizesPage = lazy(() =>
  import('./portal/package-size/LogisticsPackageSizesPage').then((module) => ({ default: module.LogisticsPackageSizesPage })),
);
const LogisticsPackageSizeFormPage = lazy(() =>
  import('./portal/package-size/LogisticsPackageSizeFormPage').then((module) => ({ default: module.LogisticsPackageSizeFormPage })),
);
const LogisticsVehiclesPage = lazy(() =>
  import('./portal/vehicle/LogisticsVehiclesPage').then((module) => ({ default: module.LogisticsVehiclesPage })),
);
const LogisticsVehicleFormPage = lazy(() =>
  import('./portal/vehicle/LogisticsVehicleFormPage').then((module) => ({ default: module.LogisticsVehicleFormPage })),
);
const LogisticsCurrenciesPage = lazy(() =>
  import('./portal/currency/LogisticsCurrenciesPage').then((module) => ({ default: module.LogisticsCurrenciesPage })),
);
const LogisticsCurrencyFormPage = lazy(() =>
  import('./portal/currency/LogisticsCurrencyFormPage').then((module) => ({ default: module.LogisticsCurrencyFormPage })),
);

function App() {
  const { isLoading } = useAuth();
  const { t } = useTranslation('common');
  const navigationItems = useLogisticsNavigationItems();

  if (isLoading) {
    return null;
  }

  return (
    <Routes>
      <Route
        path="/"
        element={(
          <Suspense fallback={null}>
            <LogisticsLandingPage />
          </Suspense>
        )}
      />
      <Route path="/portal" element={<RequireAccess hasAccess={hasLogisticsPortalAccess} />}>
        <Route
          element={(
            <PortalShell
              title={t('portal.title')}
              navigationItems={navigationItems}
              topBarRightSlot={<LanguageSwitcher />}
            />
          )}
        >
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
          <Route path="currencies" element={<LogisticsCurrenciesPage />} />
          <Route path="currencies/new" element={<LogisticsCurrencyFormPage />} />
          <Route path="currencies/:currencyId/edit" element={<LogisticsCurrencyFormPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
