import axios from 'axios';

const viteEnv = (import.meta as ImportMeta & { env?: Record<string, string | undefined> }).env;
const API_BASE_URL = viteEnv?.VITE_API_BASE_URL ?? 'http://localhost:8085';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
	if (error.response?.status === 401) {
	  window.location.href = 'http://localhost:8085/oauth2/authorization/keycloak';
	}

	return Promise.reject(error);
  },
);

export default apiClient;

