import axios from 'axios';
import { buildApiUrl, resolveApiBaseUrl } from './runtime';

const API_BASE_URL = resolveApiBaseUrl();

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
	if (error.response?.status === 401) {
    window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
	}

	return Promise.reject(error);
  },
);

export default apiClient;

