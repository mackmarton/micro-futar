import {useState} from 'react'
import './App.css'
import {apiClient} from '@package/shared-core'; // Importálod a monorepo közös csomagjából
import {Navbar} from '@package/shared-ui';

function App() {
    const [csomagok, setCsomagok] = useState([]);
    const [loading, setLoading] = useState(false);

    // Kézi bejelentkezés gomb (átirányít a Gateway-re)
    const handleLogin = () => {
        window.location.href = 'http://localhost:8085/oauth2/authorization/keycloak';
    };

    // Védett adat lekérése
    const fetchCsomagok = async () => {
        setLoading(true);
        try {
            // Ez a Gateway-en keresztül a localhost:8081-re fog menni az útvonal újraírás miatt
            const response = await apiClient.get('/api/orders/country-prices');
            setCsomagok(response.data);
        } catch (error) {
            console.error("Hiba a lekérés során", error);
            // A 401-es hibát az interceptor már lekezeli és átirányít!
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{minHeight: '100vh', backgroundColor: '#f3f4f6', margin: 0}}>
            <Navbar/>
            <div style={{padding: '20px'}}>
                <h1>📦 Csomagfeladó Rendszer</h1>

                <button onClick={handleLogin} style={{marginRight: '10px'}}>
                    Bejelentkezés
                </button>

                <button onClick={fetchCsomagok}>
                    Saját csomagjaim lekérése
                </button>

                {loading && <p>Töltés...</p>}

                <ul>
                    {csomagok.map((csomag, idx) => (
                        <li key={idx}>{csomag}</li>
                    ))}
                </ul>
            </div>
        </div>
    );
}

export default App;