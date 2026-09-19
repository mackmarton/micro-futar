import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import { HashRouter } from 'react-router-dom';
import { QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, Footer } from '@package/shared-ui';
import { createAppQueryClient } from '@package/shared-core';
import './index.css'
import App from './App.tsx'

const queryClient = createAppQueryClient();

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <QueryClientProvider client={queryClient}>
            <AuthProvider>
                <HashRouter>
                    <App/>
                    <Footer />
                </HashRouter>
            </AuthProvider>
        </QueryClientProvider>
    </StrictMode>,
)
