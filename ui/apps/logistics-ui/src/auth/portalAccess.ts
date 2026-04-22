import type { User } from '@package/shared-ui';

export const hasLogisticsPortalAccess = (user: User | null): boolean => {
  if (!user) {
    return false;
  }

  const normalizedRoles = (user.roles ?? []).map((role) => role.toLowerCase());
  return normalizedRoles.includes('logistics-admin') || normalizedRoles.includes('logistics-user');
};

