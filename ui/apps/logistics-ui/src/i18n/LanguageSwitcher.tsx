import { useEffect, useId, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { LOCALE_STORAGE_KEY, type AppLocale } from './i18n';

type LanguageOption = {
  locale: AppLocale;
  nativeName: string;
};

// Új nyelv bevezetésekor csak ezt a listát kell bővíteni.
const LANGUAGE_OPTIONS: LanguageOption[] = [
  { locale: 'hu', nativeName: 'Magyar' },
  { locale: 'en', nativeName: 'English' },
];

/**
 * Földgomb ikon, lenyitva a nyelvek saját nevükön (nem fordítva). A client-ui verziójával
 * ellentétben itt nincs útvonal-prefix: nyelvváltás runtime `i18n.changeLanguage()`-dzsel történik,
 * oldalújratöltés nélkül. A nyitás/zárás viselkedés (kattintás kívülre, Escape, aria attribútumok)
 * a shared-ui `TopNavBar` profil-menüjének mintáját követi.
 */
export const LanguageSwitcher = () => {
  const { i18n, t } = useTranslation();
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

  const handleSelectLocale = (locale: AppLocale) => {
    void i18n.changeLanguage(locale);

    try {
      window.localStorage.setItem(LOCALE_STORAGE_KEY, locale);
    } catch {
      // A localStorage elérhetetlensége (pl. privát ablak) nem akadályozza a nyelvváltást.
    }

    setIsOpen(false);
  };

  return (
    <div className="relative" ref={containerRef}>
      <button
        type="button"
        onClick={() => setIsOpen((open) => !open)}
        className="p-2 rounded-full text-on-surface hover:bg-surface-container transition-colors"
        aria-label={t('common:languageSwitcher.ariaLabel')}
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
          aria-label={t('common:languageSwitcher.label')}
          className="absolute right-0 top-12 min-w-36 rounded-xl border border-outline-variant/40 bg-white dark:bg-slate-900 shadow-lg p-1.5 z-40"
        >
          {LANGUAGE_OPTIONS.map((option) => {
            const isActive = option.locale === i18n.language;

            return (
              <button
                key={option.locale}
                type="button"
                role="menuitem"
                aria-current={isActive ? 'true' : undefined}
                onClick={() => handleSelectLocale(option.locale)}
                className={`block w-full rounded-lg px-3 py-2 text-left text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-surface-container text-on-surface'
                    : 'text-on-surface-variant hover:bg-surface-container hover:text-on-surface'
                }`}
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
