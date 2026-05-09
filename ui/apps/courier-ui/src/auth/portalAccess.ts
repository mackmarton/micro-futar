import type { User } from '@package/shared-ui';

export const hasCourierPortalAccess = (user: User | null): boolean => {
  if (!user) {
    return false;
  }

  const normalizedRoles = (user.roles ?? []).map((role) => role.toLowerCase());
  return normalizedRoles.includes('courier-admin') || normalizedRoles.includes('courier-user');
};
