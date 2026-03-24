// packages/shared-ui/src/components/Navbar.tsx
import { useAuth } from './AuthContext';

export const Navbar = () => {
    const { user, isLoading, login, logout, hasRole } = useAuth();

    return (
        <nav style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '1rem 2rem',
            backgroundColor: '#1f2937',
            color: 'white',
            boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
        }}>

            {/* Bal oldal: Logó / Márkanév */}
            <div style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>
                📦 Micro-Futár
            </div>

            {/* Középső rész: Dinamikus menüpontok (opcionális) */}
            <div style={{ display: 'flex', gap: '1.5rem' }}>
                <a href="/" style={{ color: 'white', textDecoration: 'none' }}>Főoldal</a>
                {hasRole('LOGISZTIKUS') && (
                    <a href="/admin" style={{ color: '#60a5fa', textDecoration: 'none' }}>Logisztika Admin</a>
                )}
            </div>

            {/* Jobb oldal: Felhasználói szekció */}
            <div>
                {isLoading ? (
                    <span style={{ color: '#9ca3af' }}>Betöltés...</span>
                ) : user ? (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                        <span>Szia, <strong>{user.name}</strong>!</span>
                        <button
                            onClick={logout}
                            style={{
                                padding: '0.5rem 1rem',
                                backgroundColor: '#ef4444', // Piros gomb
                                color: 'white',
                                border: 'none',
                                borderRadius: '4px',
                                cursor: 'pointer'
                            }}>
                            Kilépés
                        </button>
                    </div>
                ) : (
                    <button
                        onClick={login}
                        style={{
                            padding: '0.5rem 1rem',
                            backgroundColor: '#3b82f6', // Kék gomb
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px',
                            cursor: 'pointer'
                        }}>
                        Bejelentkezés
                    </button>
                )}
            </div>
        </nav>
    );
};