var _a;
import axios from 'axios';
var API_BASE_URL = (_a = import.meta.env.VITE_API_BASE_URL) !== null && _a !== void 0 ? _a : 'http://localhost:8085';
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
