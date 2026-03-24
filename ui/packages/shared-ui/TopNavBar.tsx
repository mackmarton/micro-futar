import type { ReactNode } from 'react';

export type TopNavLink = {
  label: string;
  href: string;
  isActive?: boolean;
};

export type TopNavBarProps = {
  title: string;
  mobileBrandName?: string;
  onNotificationsClick?: () => void;
  onProfileClick?: () => void;
  notificationsIcon?: ReactNode | string;
  profileIcon?: ReactNode | string;
  className?: string;
};

const cn = (...classes: Array<string | false | undefined>) => classes.filter(Boolean).join(' ');

const renderIcon = (icon: ReactNode | string) => {
  if (typeof icon === 'string') {
    return (
      <span className="material-symbols-outlined" aria-hidden="true">
        {icon}
      </span>
    );
  }

  return icon;
};

export const TopNavBar = ({
  title,
  mobileBrandName = 'micro-futar',
  onNotificationsClick,
  onProfileClick,
  notificationsIcon = 'notifications',
  profileIcon = 'account_circle',
  className,
}: TopNavBarProps) => {
  return (
    <header
      className={cn(
        'sticky top-0 z-30 w-full px-6 py-3 flex items-center justify-between',
        'bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl shadow-sm dark:shadow-none',
        className
      )}
    >
      <div className="flex items-center gap-4">
        <span className="lg:hidden text-xl font-headline font-bold tracking-tighter text-on-surface">{mobileBrandName}</span>
        <h2 className="hidden lg:block text-on-surface font-headline font-bold text-xl tracking-tight">{title}</h2>
      </div>

      <div className="flex items-center gap-6">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onNotificationsClick}
            className="p-2 rounded-full text-on-surface hover:bg-surface-container transition-colors"
            aria-label="Értesítések"
          >
            {renderIcon(notificationsIcon)}
          </button>
          <button
            type="button"
            onClick={onProfileClick}
            className="p-2 rounded-full text-on-surface hover:bg-surface-container transition-colors"
            aria-label="Profil"
          >
            {renderIcon(profileIcon)}
          </button>
        </div>
      </div>
    </header>
  );
};

