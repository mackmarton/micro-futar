import { Suspense, lazy } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '@package/shared-ui';

const LandingPage = lazy(() => import('./landing/LandingPage.tsx').then((module) => ({ default: module.LandingPage })));
const DashboardPage = lazy(() => import('./my-shipments/DashboardPage.tsx').then((module) => ({ default: module.DashboardPage })));
const CreateOrderPage = lazy(() => import('./create-order/CreateOrderPage.tsx').then((module) => ({ default: module.CreateOrderPage })));
const TrackPackagePage = lazy(() => import('./tracking/TrackPackagePage.tsx').then((module) => ({ default: module.TrackPackagePage })));

function App() {
  const { isLoading } = useAuth();

  if (isLoading) {
    return null;
  }

  return (
    <Suspense fallback={null}>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/portal/dashboard" element={<DashboardPage />} />
        <Route path="/portal/create-order" element={<CreateOrderPage />} />
        <Route path="/portal/tracking" element={<TrackPackagePage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}

export default App;