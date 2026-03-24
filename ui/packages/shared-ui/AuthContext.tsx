import {createContext, type ReactNode, useContext, useEffect, useState} from 'react';
import {apiClient} from '@package/shared-core';

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

    useEffect(() => {
        const fetchUser = async () => {
            try {
                const response = await apiClient.get('/api/auth/me');
                setUser(response.data);
            } catch (error) {
                // Ha 401-et kapunk, a user nincs bejelentkezve.
                // (Ha az Axios interceptorod automatikusan átirányít, akkor ez a sor le se fut,
                // de érdemes itt hagyni biztonságképpen).
                setUser(null);
            } finally {
                setIsLoading(false);
            }
        };

        fetchUser();
    }, []);

    const login = () => {
        const currentPath = window.location.pathname + window.location.search;
        localStorage.setItem('postLoginRedirect', currentPath);

        window.location.href = 'http://localhost:8085/oauth2/authorization/keycloak';
    };

    const logout = () => {
        const currentPath = window.location.pathname + window.location.search;
        localStorage.setItem('postLogoutRedirect', currentPath);

        window.location.href = 'http://localhost:8085/logout';
    };

    const hasRole = (role: string) => {
        if (!user || !user.roles) return false;
        return user.roles.map(r => r.toUpperCase()).includes(role.toUpperCase());
    };

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
                        localStorage.removeItem('postLoginRedirect');

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

    return (
        <AuthContext.Provider value={{user, isLoading, login, logout, hasRole}}>
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