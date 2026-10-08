'use client';

import Image from 'next/image';
import Link from 'next/link';
import { useTranslation } from 'react-i18next';
import { useAuth } from '@package/shared-ui';
import type { AppLocale } from '../../i18n/createI18nInstance.ts';
import { LanguageSwitcher } from '../../i18n/LanguageSwitcher.tsx';

export type LandingNavBarProps = {
    locale: AppLocale;
};

export const LandingNavBar = ({ locale }: LandingNavBarProps) => {
    const { user, login } = useAuth();
    const { t } = useTranslation(['common', 'landing']);
    const basePath = locale === 'en' ? '/en' : '';

    return (
        <nav className="fixed top-0 w-full z-50 bg-[#f8f9ff]/80 backdrop-blur-xl">
            <div className="max-w-7xl mx-auto flex items-center justify-between gap-4 px-8 py-4">
                <Link href={basePath || '/'} className="flex items-center gap-3">
                    <Image src="/micro-futar-logo.svg" alt="micro-futár logo" width={40} height={40} className="w-10 h-10"/>
                    <span className="text-2xl font-extrabold tracking-tight text-[#0b1c30]">micro-futár</span>
                </Link>

                <div className="flex items-center gap-4">
                    <LanguageSwitcher />
                    {user ? (
                        <Link
                            href={`${basePath}/portal/dashboard`}
                            className="text-on-surface text-sm font-semibold hover:text-primary transition-all"
                        >
                            {t('common:nav.dashboard')}
                        </Link>
                    ) : (
                        <button
                            type="button"
                            className="text-on-surface text-sm font-semibold hover:text-primary transition-all"
                            onClick={login}
                        >
                            {t('landing:nav.login')}
                        </button>
                    )}
                    <Link
                        href={`${basePath}/portal/create-order`}
                        className="kinetic-gradient text-on-primary px-6 py-2.5 rounded-lg font-bold text-sm active:scale-95 duration-150 shadow-lg shadow-on-surface/5"
                    >
                        {t('common:nav.createOrder')}
                    </Link>
                </div>
            </div>
        </nav>
    );
};
