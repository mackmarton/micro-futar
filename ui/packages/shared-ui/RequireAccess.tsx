import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';
import type { User } from './AuthContext';

export type RequireAccessProps = {
  hasAccess: (user: User | null) => boolean;
  redirectTo?: string;
};

export const RequireAccess = ({ hasAccess, redirectTo = '/' }: RequireAccessProps) => {
  const { user } = useAuth();

  if (!hasAccess(user)) {
    return <Navigate to={redirectTo} replace />;
  }

  return <Outlet />;
};
