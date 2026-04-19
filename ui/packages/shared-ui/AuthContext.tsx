import {createContext, type ReactNode, useCallback, useContext, useEffect, useMemo, useState} from 'react';
import {apiClient, buildApiUrl} from '@package/shared-core';

export type User = {
    name: string;
    email: string;
    preferred_username: string;
    roles: string[];
};

interface AuthContextType {
    user: User | null;
    isLoading: boolean;
    login: () => void;
    logout: () => void;
    hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({children}: { children: ReactNode }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const login = useCallback(() => {
        const currentPath = window.location.pathname + window.location.search;
        localStorage.setItem('postLoginRedirect', currentPath);

        window.location.href = buildApiUrl('/oauth2/authorization/keycloak');
    }, []);

    const logout = useCallback(() => {
        const currentPath = window.location.pathname + window.location.search;
        localStorage.setItem('postLogoutRedirect', currentPath);

        window.location.href = buildApiUrl('/logout');
    }, []);

    const hasRole = useCallback((role: string) => {
        if (!user || !user.roles) return false;
        return user.roles.map(r => r.toUpperCase()).includes(role.toUpperCase());
    }, [user]);

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const response = await apiClient.get('/api/auth/me');
                setUser(response.data);

                const postLoginRedirect = localStorage.getItem('postLoginRedirect');
                if (postLoginRedirect) {
                    localStorage.removeItem('postLoginRedirect');

                    window.location.replace(postLoginRedirect);
                } else {
                    const postLogoutRedirect = localStorage.getItem('postLogoutRedirect');
                    if (postLogoutRedirect) {
                        localStorage.removeItem('postLogoutRedirect');

                        window.location.replace(postLogoutRedirect);
                    }
                }
            } catch (error) {
                localStorage.removeItem('postLoginRedirect');
                localStorage.removeItem('postLogoutRedirect');
                setUser(null);
            } finally {
                setIsLoading(false);
            }
        };

        fetchUser();
    }, []);

    const contextValue = useMemo(
        () => ({user, isLoading, login, logout, hasRole}),
        [hasRole, isLoading, login, logout, user],
    );

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('A useAuth hook csak egy AuthProvider-en belül használható!');
    }
    return context;
};