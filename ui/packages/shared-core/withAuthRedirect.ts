import { buildApiUrl } from './runtime';

export const withAuthRedirect = async <T>(apiCall: () => Promise<T>): Promise<T> => {
  try {
    return await apiCall();
  } catch (error) {
    const status = (error as { status?: number }).status;

    if (status === 401) {
      window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }

    throw error;
  }
};
