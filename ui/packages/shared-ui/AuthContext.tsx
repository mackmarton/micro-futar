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
        const loginUrl = new URL(buildApiUrl('/oauth2/authorization/keycloak'), window.location.origin);
        loginUrl.searchParams.set('redirect_uri', window.location.href);
        window.location.href = loginUrl.toString();
    }, []);

    const logout = useCallback(() => {
        const logoutUrl = new URL(buildApiUrl('/logout'), window.location.origin);
        logoutUrl.searchParams.set('redirect_uri', window.location.href);
        window.location.href = logoutUrl.toString();
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
            } catch (error) {
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
