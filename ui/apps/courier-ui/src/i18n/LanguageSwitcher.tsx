import { useEffect, useId, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { AppLocale } from './i18n.ts';

type LanguageOption = {
  locale: AppLocale;
  nativeName: string;
};

// Új nyelv bevezetésekor csak ezt a listát kell bővíteni.
const LANGUAGE_OPTIONS: LanguageOption[] = [
  { locale: 'hu', nativeName: 'Magyar' },
  { locale: 'en', nativeName: 'English' },
];

export type LanguageSwitcherProps = {
  className?: string;
};

/**
 * Földgomb ikon, lenyitva a nyelvek saját nevükön (nem fordítva). A client-ui megfelelőjével
 * ellentétben itt nincs külön statikus oldal nyelvenként - egy futásidejű `i18next.changeLanguage`
 * hívás elég, nincs navigáció/újratöltés. A nyitás/zárás logika (kattintás a gombon kívülre,
 * Escape) a shared-ui `TopNavBar` profilmenüjének mintáját követi.
 */
export const LanguageSwitcher = ({ className }: LanguageSwitcherProps) => {
  const { i18n, t } = useTranslation('common');
  const currentLocale = i18n.language as AppLocale;

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

  const handleSelect = (locale: AppLocale) => {
    void i18n.changeLanguage(locale);
    setIsOpen(false);
  };

  return (
    <div className={className ? `relative ${className}` : 'relative'} ref={containerRef}>
      <button
        type="button"
        onClick={() => setIsOpen((open) => !open)}
        className="p-2 rounded-full text-on-surface hover:bg-surface-container transition-colors"
        aria-label={t('languageSwitcher.ariaLabel')}
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
          aria-label={t('languageSwitcher.menuLabel')}
          className="absolute right-0 top-12 z-40 min-w-36 max-w-[calc(100vw-2rem)] rounded-xl border border-outline-variant/40 bg-white dark:bg-slate-900 shadow-lg p-1.5"
        >
          {LANGUAGE_OPTIONS.map((option) => {
            const isActive = option.locale === currentLocale;

            return (
              <button
                key={option.locale}
                type="button"
                role="menuitem"
                aria-current={isActive ? 'true' : undefined}
                onClick={() => handleSelect(option.locale)}
                className={
                  isActive
                    ? 'block w-full rounded-lg px-3 py-2 text-left text-sm font-medium transition-colors bg-surface-container text-on-surface'
                    : 'block w-full rounded-lg px-3 py-2 text-left text-sm font-medium transition-colors text-on-surface-variant hover:bg-surface-container hover:text-on-surface'
                }
              >
                {option.nativeName}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
};
