import type {ReactNode} from 'react';
import { useAuth } from './AuthContext';

export type NavigationItem = {
    label: string;
    href: string;
    icon?: ReactNode | string;
    isActive?: boolean;
    onlyLoggedIn?: boolean;
};

export type SideNavBarProps = {
    navigationItems: NavigationItem[];
    brandName?: string;
    brandSubtitle?: string;
    className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const renderIcon = (icon?: ReactNode | string) => {
    if (!icon) return null;

    if (typeof icon === 'string') {
        return <span className="material-symbols-outlined" aria-hidden="true">{icon}</span>;
    }

    return icon;
};

export const SideNavBar = ({
                               navigationItems,
                               brandName = 'micro-futár',
                               brandSubtitle = 'Logisztikai Portál',
                               className,
                           }: SideNavBarProps) => {
    const { user } = useAuth();
    const visibleNavigationItems = navigationItems.filter((item) => !item.onlyLoggedIn || Boolean(user));

    return (
        <aside
            className={cn('h-screen w-64 hidden lg:flex flex-col fixed left-0 top-0 p-4 gap-2 z-40 bg-surface-container-low', className)}>
            <a href="/" className="mb-8 px-2 flex items-center gap-3">
                <img src="/micro-futar-logo.svg" alt="micro-futár logo" className="w-10 h-10 shrink-0"/>
                <div>
                    <h1 className="font-headline text-lg font-black tracking-tighter text-on-surface">{brandName}</h1>
                    <p className="text-xs font-medium text-on-surface-variant opacity-70">{brandSubtitle}</p>
                </div>
            </a>

            <nav className="flex flex-col gap-2" aria-label="Oldalsó navigáció">
                {visibleNavigationItems.map((item) => (
                    <a
                        key={`${item.href}-${item.label}`}
                        href={item.href}
                        className={cn(
                            'flex items-center gap-3 p-3 rounded-lg font-label transition-all duration-300',
                            item.isActive
                                ? 'bg-surface-container-lowest dark:bg-slate-900 text-teal-600 dark:text-teal-400 shadow-sm font-semibold'
                                : 'text-on-surface-variant hover:translate-x-1 hover:bg-surface-container-high/60 dark:hover:bg-slate-800/50'
                        )}
                        aria-current={item.isActive ? 'page' : undefined}
                    >
                        {renderIcon(item.icon)}
                        <span className="font-medium">{item.label}</span>
                    </a>
                ))}
            </nav>
        </aside>
    );
};

