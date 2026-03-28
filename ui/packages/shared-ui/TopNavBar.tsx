import { type ReactNode, useEffect, useId, useRef, useState } from 'react';
import { useAuth } from './AuthContext';

export type TopNavLink = {
  label: string;
  href: string;
  isActive?: boolean;
};

export type TopNavBarProps = {
  title: string;
  mobileBrandName?: string;
  onProfileClick?: () => void;
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
  onProfileClick,
  profileIcon = 'account_circle',
  className,
}: TopNavBarProps) => {
  const { user, isLoading, login, logout } = useAuth();
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const profileMenuId = useId();
  const profileMenuRef = useRef<HTMLDivElement | null>(null);
  const greetingName = user?.name || user?.preferred_username;

  useEffect(() => {
    const handleOutsideClick = (event: MouseEvent) => {
      if (!profileMenuRef.current?.contains(event.target as Node)) {
        setIsProfileMenuOpen(false);
      }
    };

    const handleEscapePress = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setIsProfileMenuOpen(false);
      }
    };

    if (isProfileMenuOpen) {
      document.addEventListener('mousedown', handleOutsideClick);
      document.addEventListener('keydown', handleEscapePress);
    }

    return () => {
      document.removeEventListener('mousedown', handleOutsideClick);
      document.removeEventListener('keydown', handleEscapePress);
    };
  }, [isProfileMenuOpen]);

  const handleProfileToggle = () => {
    onProfileClick?.();
    setIsProfileMenuOpen((isOpen) => !isOpen);
  };

  const handleAuthAction = () => {
    setIsProfileMenuOpen(false);
    if (user) {
      logout();
      return;
    }
    login();
  };

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
        <div className="relative flex items-center gap-3" ref={profileMenuRef}>
          <button
            type="button"
            onClick={handleProfileToggle}
            className="p-2 rounded-full text-on-surface hover:bg-surface-container transition-colors"
            aria-label="Profil"
            aria-haspopup="menu"
            aria-expanded={isProfileMenuOpen}
            aria-controls={profileMenuId}
          >
            {renderIcon(profileIcon)}
          </button>

          {isProfileMenuOpen && (
            <div
              id={profileMenuId}
              role="menu"
              aria-label="Profil menü"
              className="absolute right-0 top-12 min-w-56 rounded-xl border border-outline-variant/40 bg-white dark:bg-slate-900 shadow-lg p-3"
            >
              {user && greetingName && (
                <p className="text-sm font-medium text-on-surface">Üdv, {greetingName}!</p>
              )}
              <button
                type="button"
                role="menuitem"
                onClick={handleAuthAction}
                disabled={isLoading}
                className={cn(
                  'w-full rounded-lg px-3 py-2 text-left text-sm font-medium text-on-surface hover:bg-surface-container transition-colors disabled:opacity-60 disabled:cursor-not-allowed',
                  Boolean(user && greetingName) && 'mt-3'
                )}
              >
                {user ? 'Kijelentkezés' : 'Bejelentkezés'}
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

