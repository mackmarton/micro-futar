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
          <Route path="shipment-pickup" element={<CourierDashboardPage />} />
          <Route index element={<Navigate to="/portal/shipment-pickup" replace />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
