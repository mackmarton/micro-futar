/* eslint-disable @typescript-eslint/no-unused-vars */
import { i18nBuilder } from "keycloakify/login";
import type { ThemeName } from "../kc.gen";

/**
 * Saját, téma-specifikus üzenetkulcsok (a beépített Keycloak szótárban nincs pont ilyen rövid
 * "Bejelentkezés"/"Login" cím – a `loginAccountTitle` egy teljes mondat), lásd:
 * https://docs.keycloakify.dev/features/i18n#customizing-translations
 */
const { useI18n, ofTypeI18n } = i18nBuilder
    .withThemeName<ThemeName>()
    .withCustomTranslations({
        hu: { loginPageHeading: "Bejelentkezés" },
        en: { loginPageHeading: "Login" },
    })
    .build();

type I18n = typeof ofTypeI18n;

export { useI18n, type I18n };
