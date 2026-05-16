import { Suspense, lazy } from 'react';
import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { useAuth } from '@package/shared-ui';
import { hasCourierPortalAccess } from './auth/portalAccess';
import { CourierPortalShell } from './portal/CourierPortalShell';

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

const RequireCourierAccess = () => {
  const { user } = useAuth();
  const isAuthorized = hasCourierPortalAccess(user);

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
    <Routes>
      <Route
        path="/"
        element={
          <Suspense fallback={null}>
            <CourierLandingPage />
          </Suspense>
        }
      />
      <Route path="/portal" element={<RequireCourierAccess />}>
        <Route element={<CourierPortalShell />}>
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
