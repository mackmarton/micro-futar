import {Suspense, lazy} from "react";
import type {ClassKey} from "keycloakify/login";
import type {KcContext} from "./KcContext";
import {useI18n} from "./i18n";
import DefaultPage from "keycloakify/login/DefaultPage";
import Template from "keycloakify/login/Template";
import "../index.css";
import Register from "./pages/Register.tsx";
import {LanguageSwitcher} from "./LanguageSwitcher.tsx";

const Login = lazy(() => import("./pages/Login"));
const UserProfileFormFields = lazy(
    () => import("keycloakify/login/UserProfileFormFields")
);

const doMakeUserConfirmPassword = true;

export default function KcPage(props: { kcContext: KcContext }) {
    const {kcContext} = props;

    const {i18n} = useI18n({kcContext});

    return (
        <Suspense>
            {/*
                A Keycloak beépített `kc-locale` dropdownja (a `Template`-en belül, lásd
                `index.css`-ben a `#kc-locale { display: none }`-t) stílus nélkül, egymás alatt
                listázva jelenne meg (`doUseDefaultCss={false}`), ezért CSS-sel elrejtjük, és
                helyette itt, a Template-től függetlenül, saját földgombos változót renderelünk.
            */}
            {i18n.enabledLanguages.length > 1 && (
                <div className="fixed top-4 right-4 z-50">
                    <LanguageSwitcher currentLanguageLabel={i18n.currentLanguage.label} languages={i18n.enabledLanguages} />
                </div>
            )}
            {(() => {
                switch (kcContext.pageId) {
                    case "login.ftl":
                        return (
                            <Login
                                {...{kcContext, i18n, classes, Template, doUseDefaultCss: false}}
                            />
                        );
                    case "register.ftl":
                        return (
                            <Register
                                {...{
                                    kcContext,
                                    i18n,
                                    classes,
                                    Template,
                                    doUseDefaultCss: false,
                                    UserProfileFormFields,
                                    doMakeUserConfirmPassword
                                }}
                            />
                        );
                    default:
                        return (
                            <DefaultPage
                                kcContext={kcContext}
                                i18n={i18n}
                                classes={classes}
                                Template={Template}
                                doUseDefaultCss={false}
                                UserProfileFormFields={UserProfileFormFields}
                                doMakeUserConfirmPassword={doMakeUserConfirmPassword}
                            />
                        );
                }
            })()}
        </Suspense>
    );
}

const classes = {} satisfies { [key in ClassKey]?: string };