import { useEffect, useId, useRef, useState } from "react";
import { cn } from "@package/shared-ui/cn";

export type LanguageSwitcherProps = {
    currentLanguageLabel: string;
    languages: { languageTag: string; label: string; href: string }[];
};

/**
 * A Keycloak beépített locale-dropdownja (`keycloakify/login/Template`) a projekt saját
 * Tailwind-témájában stílus nélkül, egymás alatt listázva jelenik meg (`doUseDefaultCss={false}`,
 * nincs a Keycloak alap `login.css` betöltve). Ez a földgombos verzió ugyanazt a mintát követi,
 * mint a többi app `LanguageSwitcher`-je (lásd pl. `apps/client-ui/src/i18n/LanguageSwitcher.tsx`),
 * de a nyelvváltás itt is a Keycloak által adott `href`-re navigálással történik, nem runtime
 * állapotváltással (a bejelentkező oldal szerver-renderelt, nincs saját i18next instance).
 */
export const LanguageSwitcher = ({ currentLanguageLabel, languages }: LanguageSwitcherProps) => {
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
            if (event.key === "Escape") {
                setIsOpen(false);
            }
        };

        if (isOpen) {
            document.addEventListener("mousedown", handleOutsideClick);
            document.addEventListener("keydown", handleEscapePress);
        }

        return () => {
            document.removeEventListener("mousedown", handleOutsideClick);
            document.removeEventListener("keydown", handleEscapePress);
        };
    }, [isOpen]);

    if (languages.length <= 1) {
        return null;
    }

    return (
        <div className="relative" ref={containerRef}>
            <button
                type="button"
                onClick={() => setIsOpen(open => !open)}
                className="p-2 rounded-full text-on-surface hover:bg-surface-container transition-colors"
                aria-label={currentLanguageLabel}
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
                    {languages.map(language => {
                        const isActive = language.label === currentLanguageLabel;

                        return (
                            <a
                                key={language.languageTag}
                                href={language.href}
                                role="menuitem"
                                aria-current={isActive ? "true" : undefined}
                                onClick={() => setIsOpen(false)}
                                className={cn(
                                    "block w-full rounded-lg px-3 py-2 text-left text-sm font-medium transition-colors",
                                    isActive
                                        ? "bg-surface-container text-on-surface"
                                        : "text-on-surface-variant hover:bg-surface-container hover:text-on-surface"
                                )}
                            >
                                {language.label}
                            </a>
                        );
                    })}
                </div>
            )}
        </div>
    );
};
