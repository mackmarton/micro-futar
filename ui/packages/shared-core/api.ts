import axios from 'axios';
import { CSRF_HEADER_NAME, CSRF_HEADER_VALUE, reportCsrfRejection } from './csrf';
import { buildApiUrl, resolveApiBaseUrl } from './runtime';

const API_BASE_URL = resolveApiBaseUrl();

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: { [CSRF_HEADER_NAME]: CSRF_HEADER_VALUE },
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    reportCsrfRejection(error.response?.status, error.response?.data, error.config?.url);

	if (error.response?.status === 401) {
    window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
	}

	return Promise.reject(error);
  },
);

export default apiClient;

