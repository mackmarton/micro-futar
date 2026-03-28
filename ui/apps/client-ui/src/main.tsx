import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import { HashRouter } from 'react-router-dom';
import { Footer } from '@package/shared-ui';
import './index.css'
import App from './App.tsx'
import { AuthProvider } from '@package/shared-ui';

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <AuthProvider>
            <HashRouter>
                <App/>
                <Footer />
            </HashRouter>
        </AuthProvider>
    </StrictMode>,
)
