import {Suspense, lazy} from "react";
import type {ClassKey} from "keycloakify/login";
import type {KcContext} from "./KcContext";
import {useI18n} from "./i18n";
import DefaultPage from "keycloakify/login/DefaultPage";
import Template from "keycloakify/login/Template";
import "../index.css";
import Register from "./pages/Register.tsx";

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