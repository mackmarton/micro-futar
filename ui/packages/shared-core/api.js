import axios from 'axios';
var resolveApiBaseUrl = function () {
    var _a, _b;
    var globalEnv = ((_a = globalThis.__APP_ENV__) !== null && _a !== void 0 ? _a : {});
    var processEnv = ((_b = globalThis.process) === null || _b === void 0 ? void 0 : _b.env) || {};
    return globalEnv.VITE_API_BASE_URL ||
        globalEnv.API_BASE_URL ||
        processEnv.VITE_API_BASE_URL ||
        processEnv.API_BASE_URL ||
        'http://localhost:8085';
};
var API_BASE_URL = resolveApiBaseUrl();
export var apiClient = axios.create({
    baseURL: API_BASE_URL,
    withCredentials: true,
});
apiClient.interceptors.response.use(function (response) { return response; }, function (error) {
    var _a;
    if (((_a = error.response) === null || _a === void 0 ? void 0 : _a.status) === 401) {
        window.location.href = 'http://localhost:8085/oauth2/authorization/keycloak';
    }
    return Promise.reject(error);
});
export default apiClient;
