import { Suspense, lazy } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { PortalShell, RequireAccess, useAuth } from '@package/shared-ui';
import { hasCourierPortalAccess } from './auth/portalAccess';
import { courierNavigationItems } from './portal/navigation';

const CourierLandingPage = lazy(() =>
  import('./landing/CourierLandingPage').then((module) => ({ default: module.CourierLandingPage })),
);
const CourierDashboardPage = lazy(() =>
  import('./portal/pickup/CourierPickupPage').then((module) => ({ default: module.CourierPickupPage })),
);
const CourierDropoffPage = lazy(() =>
  import('./portal/dropoff/CourierDropoffPage').then((module) => ({ default: module.CourierDropoffPage })),
);
const CourierAllocatedPackagesPage = lazy(() =>
  import('./portal/allocations/CourierAllocatedPackagesPage').then((module) => ({
    default: module.CourierAllocatedPackagesPage,
  })),
);
const CourierAllocatedPackageDetailsPage = lazy(() =>
  import('./portal/allocations/CourierAllocatedPackageDetailsPage').then((module) => ({
    default: module.CourierAllocatedPackageDetailsPage,
  })),
);

function App() {
  const { isLoading } = useAuth();

  if (isLoading) {
    return null;
  }

  return (
    <Routes>
      <Route
        path="/"
        element={
          <Suspense fallback={null}>
            <CourierLandingPage />
          </Suspense>
        }
      />
      <Route path="/portal" element={<RequireAccess hasAccess={hasCourierPortalAccess} />}>
        <Route element={<PortalShell title="Futár" navigationItems={courierNavigationItems} />}>
          <Route path="allocated-packages" element={<CourierAllocatedPackagesPage />} />
          <Route path="allocated-packages/:assignmentId" element={<CourierAllocatedPackageDetailsPage />} />
          <Route path="shipment-pickup" element={<CourierDashboardPage />} />
          <Route path="shipment-dropoff" element={<CourierDropoffPage />} />
          <Route index element={<Navigate to="/portal/allocated-packages" replace />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
