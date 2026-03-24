import type { ReactNode } from 'react';

export type NavigationItem = {
  label: string;
  href: string;
  icon?: ReactNode | string;
  isActive?: boolean;
};

export type SideNavBarProps = {
  navigationItems: NavigationItem[];
  brandName?: string;
  brandSubtitle?: string;
  ctaHref?: string;
  onCtaClick?: () => void;
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
  brandName = 'micro-futar',
  brandSubtitle = 'Logistics Portal',
  ctaHref,
  onCtaClick,
  className,
}: SideNavBarProps) => {
  const ctaButtonClasses = cn(
    'w-full rounded-lg px-4 py-3 font-bold flex items-center justify-center gap-2',
    'bg-primary text-on-primary transition-all active:scale-95',
    'hover:bg-gradient-to-r hover:from-primary hover:to-on-primary-container'
  );

  return (
    <aside className={cn('h-screen w-64 hidden lg:flex flex-col fixed left-0 top-0 p-4 gap-2 z-40 bg-surface-container-low', className)}>
      <div className="mb-8 px-2">
        <h1 className="font-headline text-lg font-black tracking-tighter text-on-surface">{brandName}</h1>
        <p className="text-xs font-medium text-on-surface-variant opacity-70">{brandSubtitle}</p>
      </div>

      <nav className="flex flex-col gap-2" aria-label="Oldalsó navigáció">
        {navigationItems.map((item) => (
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

