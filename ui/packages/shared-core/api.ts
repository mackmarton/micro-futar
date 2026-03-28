import axios from 'axios';

type RuntimeEnv = {
  VITE_API_BASE_URL?: string;
  API_BASE_URL?: string;
};

const resolveApiBaseUrl = () => {
  const globalEnv = ((globalThis as { __APP_ENV__?: RuntimeEnv }).__APP_ENV__) ?? {};
  const processEnv = ((globalThis as { process?: { env?: RuntimeEnv } }).process?.env ?? {});

  return (
    globalEnv.VITE_API_BASE_URL ??
    globalEnv.API_BASE_URL ??
    processEnv.VITE_API_BASE_URL ??
    processEnv.API_BASE_URL ??
    'http://localhost:8085'
  );
};

const API_BASE_URL = resolveApiBaseUrl();

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

