import {useState} from "react";
import {kcSanitize} from "keycloakify/lib/kcSanitize";
import type {PageProps} from "keycloakify/login/pages/PageProps";
import type {KcContext} from "../KcContext";
import type {I18n} from "../i18n";
import {useScript} from "keycloakify/login/pages/Login.useScript";
import icon from "../../assets/micro-futar-logo.svg";
import { PrecisionInput } from "@package/shared-ui/forms/PrecisionInput";

export default function Login(props: PageProps<Extract<KcContext, { pageId: "login.ftl" }>, I18n>) {
    const {kcContext, i18n, doUseDefaultCss, Template, classes} = props;

    const TemplateComponent = Template as any;

    const {
        social,
        realm,
        url,
        usernameHidden,
        login,
        auth,
        registrationDisabled,
        messagesPerField,
        enableWebAuthnConditionalUI,
        authenticators
    } = kcContext;

    const {msg, msgStr} = i18n;

    const [isLoginButtonDisabled, setIsLoginButtonDisabled] = useState(false);
    const [usernameValue, setUsernameValue] = useState(login.username ?? "");
    const [passwordValue, setPasswordValue] = useState("");

    const webAuthnButtonId = "authenticateWebAuthnButton";

    useScript({
        webAuthnButtonId,
        kcContext,
        i18n
    });

    return (
        <TemplateComponent
            kcContext={kcContext}
            i18n={i18n}
            doUseDefaultCss={doUseDefaultCss}
            classes={classes}
            displayMessage={false}
            headerNode={null}
            displayInfo={false}
            infoNode={
                <div className="mt-6 text-center text-sm text-on-surface-variant">
                    <span>
                        {msg("noAccount")} {" "}
                        <a tabIndex={8} href={url.registrationUrl}
                           className="font-semibold text-on-primary-container hover:underline">
                            {msg("doRegister")}
                        </a>
                    </span>
                </div>
            }
            socialProvidersNode={
                <>
                    {realm.password && social?.providers !== undefined && social.providers.length !== 0 && (
                        <div className="mt-6">
                            <div className="flex items-center gap-3 mb-4">
                                <div className="h-px flex-1 bg-surface-container-high"/>
                                <h2 className="text-xs font-semibold uppercase tracking-widest text-on-surface-variant">
                                    {msg("identity-provider-login-label")}
                                </h2>
                                <div className="h-px flex-1 bg-surface-container-high"/>
                            </div>
                            <ul className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                                {social.providers.map((p) => (
                                    <li key={p.alias}>
                                        <a
                                            id={`social-${p.alias}`}
                                            className="block w-full rounded-xl bg-surface-container-low px-4 py-3 text-sm font-semibold text-on-surface hover:bg-surface-container-high transition-colors"
                                            href={p.loginUrl}
                                        >
                                            <span dangerouslySetInnerHTML={{__html: kcSanitize(p.displayName)}}/>
                                        </a>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </>
            }
        >
            <main
                className="min-h-screen bg-surface-container-low px-4 py-10 md:py-16 flex items-center justify-center">
                <section
                    className="w-full max-w-md bg-surface-container-lowest rounded-2xl p-8 shadow-2xl shadow-on-surface/10">
                    <header className="mb-8">
                        <div className="flex items-center gap-3 mb-6">
                            <img src={icon} alt="micro-futár logó" className="w-8 h-8"/>
                            <span className="text-xl font-bold text-on-surface">micro-futár</span>
                        </div>
                        <h1 className="font-headline text-3xl font-extrabold text-on-surface">Bejelentkezés</h1>
                    </header>

                    {realm.password && (
                        <form
                            id="kc-form-login"
                            onSubmit={() => {
                                setIsLoginButtonDisabled(true);
                                return true;
                            }}
                            action={url.loginAction}
                            method="post"
                            className="space-y-6"
                        >
                            {!usernameHidden && (
                                <div>
                                    <PrecisionInput
                                        label={
                                            !realm.loginWithEmailAllowed
                                                ? msgStr("username")
                                                : !realm.registrationEmailAsUsername
                                                    ? msgStr("usernameOrEmail")
                                                    : msgStr("email")
                                        }
                                        id="username"
                                        name="username"
                                        type="text"
                                        value={usernameValue}
                                        autoComplete={enableWebAuthnConditionalUI ? "username webauthn" : "username"}
                                        onChange={(event) => setUsernameValue(event.target.value)}
                                        className="rounded-none px-0 py-3 border-b-2 border-surface-container-high"
                                    />
                                    {messagesPerField.existsError("username", "password") && (
                                        <p
                                            id="input-error"
                                            className="mt-2 text-sm text-error"
                                            aria-live="polite"
                                            dangerouslySetInnerHTML={{
                                                __html: kcSanitize(messagesPerField.getFirstError("username", "password"))
                                            }}
                                        />
                                    )}
                                </div>
                            )}

                            <div>
                                <PrecisionInput
                                    label={msgStr("password")}
                                    id="password"
                                    name="password"
                                    type="password"
                                    value={passwordValue}
                                    autoComplete="current-password"
                                    onChange={(event) => setPasswordValue(event.target.value)}
                                    className="rounded-none px-0 py-3 border-b-2 border-surface-container-high"
                                />
                                {usernameHidden && messagesPerField.existsError("username", "password") && (
                                    <p
                                        id="input-error"
                                        className="mt-2 text-sm text-error"
                                        aria-live="polite"
                                        dangerouslySetInnerHTML={{
                                            __html: kcSanitize(messagesPerField.getFirstError("username", "password"))
                                        }}
                                    />
                                )}
                            </div>

                            <div className="flex items-center justify-between gap-4 text-sm">
                                <div>
                                    {realm.rememberMe && !usernameHidden && (
                                        <label className="inline-flex items-center gap-2 text-on-surface-variant">
                                            <input
                                                tabIndex={5}
                                                id="rememberMe"
                                                name="rememberMe"
                                                type="checkbox"
                                                defaultChecked={!!login.rememberMe}
                                                className="rounded border-outline-variant text-surface-tint focus:ring-surface-tint"
                                            />
                                            {msg("rememberMe")}
                                        </label>
                                    )}
                                </div>
                                <div>
                                    {realm.resetPasswordAllowed && (
                                        <a tabIndex={6} href={url.loginResetCredentialsUrl}
                                           className="font-semibold text-on-primary-container hover:underline">
                                            {msg("doForgotPassword")}
                                        </a>
                                    )}
                                </div>
                            </div>

                            <div>
                                <input type="hidden" id="id-hidden-input" name="credentialId"
                                       value={auth.selectedCredential}/>
                                <input
                                    tabIndex={7}
                                    disabled={isLoginButtonDisabled}
                                    name="login"
                                    id="kc-login"
                                    type="submit"
                                    value={msgStr("doLogIn")}
                                    className="kinetic-gradient text-on-primary w-full rounded-xl bg-primary px-6 py-3 text-sm font-bold transition-opacity hover:opacity-90 disabled:opacity-60"
                                />
                            </div>

                            {!registrationDisabled && realm.registrationAllowed && (
                                <p className="mt-2 text-center text-sm text-on-surface-variant">
                                    {msg("noAccount")} {" "}
                                    <a tabIndex={8} href={url.registrationUrl}
                                       className="font-semibold text-on-primary-container hover:underline">
                                        {msg("doRegister")}
                                    </a>
                                </p>
                            )}
                        </form>
                    )}

                    {enableWebAuthnConditionalUI && (
                        <div className="mt-6">
                            <form id="webauth" action={url.loginAction} method="post">
                                <input type="hidden" id="clientDataJSON" name="clientDataJSON"/>
                                <input type="hidden" id="authenticatorData" name="authenticatorData"/>
                                <input type="hidden" id="signature" name="signature"/>
                                <input type="hidden" id="credentialId" name="credentialId"/>
                                <input type="hidden" id="userHandle" name="userHandle"/>
                                <input type="hidden" id="error" name="error"/>
                            </form>

                            {authenticators !== undefined && authenticators.authenticators.length !== 0 && (
                                <form id="authn_select">
                                    {authenticators.authenticators.map((authenticator, i) => (
                                        <input key={i} type="hidden" name="authn_use_chk" readOnly
                                               value={authenticator.credentialId}/>
                                    ))}
                                </form>
                            )}

                            <input
                                id={webAuthnButtonId}
                                type="button"
                                value={msgStr("passkey-doAuthenticate")}
                                className="mt-4 w-full rounded-xl border border-outline-variant bg-surface-container-low px-6 py-3 text-sm font-semibold text-on-surface hover:bg-surface-container-high transition-colors"
                            />
                        </div>
                    )}
                </section>
            </main>
        </TemplateComponent>
    );
}
