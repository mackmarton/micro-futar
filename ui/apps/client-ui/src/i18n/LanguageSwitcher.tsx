'use client';

import { useEffect, useId, useRef, useState } from 'react';
import { usePathname } from 'next/navigation';
import { cn } from '@package/shared-ui/cn';
import type { AppLocale } from './createI18nInstance.ts';

const EN_PREFIX = '/en';

type LanguageOption = {
  locale: AppLocale;
  nativeName: string;
};

// Új nyelv bevezetésekor csak ezt a listát kell bővíteni.
const LANGUAGE_OPTIONS: LanguageOption[] = [
  { locale: 'hu', nativeName: 'Magyar' },
  { locale: 'en', nativeName: 'English' },
];

const pathWithoutEnPrefix = (pathname: string) => {
  const isEnglish = pathname === EN_PREFIX || pathname.startsWith(`${EN_PREFIX}/`);
  return isEnglish ? pathname.slice(EN_PREFIX.length) || '/' : pathname;
};

const pathForLocale = (basePath: string, locale: AppLocale) =>
  locale === 'en' ? (basePath === '/' ? EN_PREFIX : `${EN_PREFIX}${basePath}`) : basePath;

export type LanguageSwitcherProps = {
  className?: string;
};

/**
 * Földgomb ikon, lenyitva a nyelvek saját nevükön (nem fordítva). Mivel a `(hu)` és az `en/`
 * külön gyökér-layout (eltérő `<html lang>`-gel), a köztük navigálás mindig teljes oldalbetöltés
 * – ezért sima `<a>`, nem a Next `<Link>` (konzisztens a shared-ui nav linkekkel).
 */
export const LanguageSwitcher = ({ className }: LanguageSwitcherProps) => {
  const pathname = usePathname() ?? '/';
  const basePath = pathWithoutEnPrefix(pathname);
  const currentLocale: AppLocale = pathname === EN_PREFIX || pathname.startsWith(`${EN_PREFIX}/`) ? 'en' : 'hu';

  const [isOpen, setIsOpen] = useState(false);
  const menuId = useId();
  const containerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const handleOutsideClick = (event: MouseEvent) => {
      if (!containerRef.current?.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    const handleEscapePress = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleOutsideClick);
      document.addEventListener('keydown', handleEscapePress);
    }

    return () => {
      document.removeEventListener('mousedown', handleOutsideClick);
      document.removeEventListener('keydown', handleEscapePress);
    };
  }, [isOpen]);

  return (
    <div className={cn('relative', className)} ref={containerRef}>
      <button
        type="button"
        onClick={() => setIsOpen((open) => !open)}
        className="p-2 rounded-full text-on-surface hover:bg-surface-container transition-colors"
        aria-label="Nyelv kiválasztása / Select language"
        aria-haspopup="menu"
        aria-expanded={isOpen}
        aria-controls={menuId}
      >
        <span className="material-symbols-outlined" aria-hidden="true">
          language
        </span>
      </button>

      {isOpen && (
        <div
          id={menuId}
          role="menu"
          aria-label="Nyelv / Language"
          className="absolute right-0 top-12 min-w-36 rounded-xl border border-outline-variant/40 bg-white dark:bg-slate-900 shadow-lg p-1.5 z-40"
        >
          {LANGUAGE_OPTIONS.map((option) => {
            const isActive = option.locale === currentLocale;

            return (
              <a
                key={option.locale}
                href={pathForLocale(basePath, option.locale)}
                role="menuitem"
                aria-current={isActive ? 'true' : undefined}
                onClick={() => setIsOpen(false)}
                className={cn(
                  'block w-full rounded-lg px-3 py-2 text-left text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-surface-container text-on-surface'
                    : 'text-on-surface-variant hover:bg-surface-container hover:text-on-surface'
                )}
              >
                {option.nativeName}
              </a>
            );
          })}
        </div>
      )}
    </div>
  );
};
