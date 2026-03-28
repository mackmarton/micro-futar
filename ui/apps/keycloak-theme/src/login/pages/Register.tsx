import type { JSX } from "keycloakify/tools/JSX";
import { useCallback, useLayoutEffect, useState } from "react";
import type { LazyOrNot } from "keycloakify/tools/LazyOrNot";
import { kcSanitize } from "keycloakify/lib/kcSanitize";
import type { UserProfileFormFieldsProps } from "keycloakify/login/UserProfileFormFieldsProps";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../KcContext";
import type { I18n } from "../i18n";
import icon from "../../assets/micro-futar-logo.svg";
import { PrecisionInput } from "@package/shared-ui/forms/PrecisionInput";

type RegisterProps = PageProps<Extract<KcContext, { pageId: "register.ftl" }>, I18n> & {
    UserProfileFormFields: LazyOrNot<(props: UserProfileFormFieldsProps) => JSX.Element>;
    doMakeUserConfirmPassword: boolean;
};

export default function Register(props: RegisterProps) {
    const { kcContext, i18n, doUseDefaultCss, Template, classes, UserProfileFormFields, doMakeUserConfirmPassword } = props;
    const TemplateComponent = Template as any;
    const UserProfileFormFieldsComponent = UserProfileFormFields as any;

    const { messageHeader, url, messagesPerField, recaptchaRequired, recaptchaVisible, recaptchaSiteKey, recaptchaAction, termsAcceptanceRequired } =
        kcContext;

    const { msg, msgStr } = i18n;

    const classMap: Record<string, string> = {
        kcFormClass: "space-y-6",
        kcFormGroupClass: "space-y-2",
        kcLabelWrapperClass: "hidden",
        kcLabelClass: "block text-sm font-medium text-on-surface",
        kcInputWrapperClass: "hidden",
        kcInputClass: "hidden",
        kcInputErrorMessageClass: "hidden",
        kcInputGroup: "hidden",
        kcFormPasswordVisibilityButtonClass:
            "absolute right-0 top-1/2 -translate-y-1/2 text-on-surface-variant hover:text-on-surface transition-colors",
        kcFormPasswordVisibilityIconShow: "material-symbols-outlined",
        kcFormPasswordVisibilityIconHide: "material-symbols-outlined",
        kcInputHelperTextBeforeClass: "text-xs text-on-surface-variant",
        kcInputHelperTextAfterClass: "text-xs text-on-surface-variant",
        kcContentWrapperClass: "mb-2",
        kcFormGroupHeader: "text-sm font-semibold text-on-surface",
        kcInputClassRadio: "flex items-center gap-2",
        kcInputClassRadioInput: "text-surface-tint focus:ring-surface-tint",
        kcInputClassRadioLabel: "text-sm text-on-surface",
        kcInputClassCheckbox: "flex items-center gap-2",
        kcInputClassCheckboxInput: "rounded border-outline-variant text-surface-tint focus:ring-surface-tint",
        kcInputClassCheckboxLabel: "text-sm text-on-surface",
        kcInputClassRadioCheckboxLabelDisabled: "opacity-60",
        kcFormOptionsClass: "",
        kcFormOptionsWrapperClass: "",
        kcFormButtonsClass: "",
        kcButtonClass: "",
        kcButtonPrimaryClass: "",
        kcButtonBlockClass: "",
        kcButtonLargeClass: ""
    };

    const kcClsx = (...classKeys: Array<string | false | null | undefined>) =>
        classKeys
            .filter((classKey): classKey is string => Boolean(classKey))
            .map(classKey => classMap[classKey] ?? "")
            .filter(Boolean)
            .join(" ");

    const renderPrecisionField = useCallback<NonNullable<UserProfileFormFieldsProps["BeforeField"]>>(
        ({
            attribute,
            dispatchFormAction,
            displayableErrors,
            valueOrValues,
            i18n
        }) => {
            const inputType = attribute.annotations.inputType;

            if (inputType === "hidden" || valueOrValues instanceof Array) {
                return null;
            }

            const normalizedType = inputType?.startsWith("html5-") ? inputType.slice(6) : inputType;
            const isPasswordField =
                attribute.name === "password" ||
                attribute.name === "password-confirm" ||
                attribute.autocomplete === "new-password" ||
                attribute.autocomplete === "current-password";
            const resolvedType = normalizedType ?? (isPasswordField ? "password" : "text");
            const supportedTypes = new Set([undefined, "text", "email", "password", "tel", "number", "url"]);

            if (!supportedTypes.has(resolvedType)) {
                return null;
            }

            const advancedMsgStr = (i18n as any).advancedMsgStr as ((key: string) => string) | undefined;
            const label = advancedMsgStr?.(attribute.displayName ?? "") ?? attribute.displayName ?? attribute.name;

            return (
                <div className="space-y-2">
                    <PrecisionInput
                        label={label}
                        id={attribute.name}
                        name={attribute.name}
                        type={resolvedType}
                        value={valueOrValues}
                        autoComplete={attribute.autocomplete}
                        disabled={attribute.readOnly}
                        onChange={event =>
                            dispatchFormAction({
                                action: "update",
                                name: attribute.name,
                                valueOrValues: event.target.value
                            })
                        }
                        onBlur={() =>
                            dispatchFormAction({
                                action: "focus lost",
                                name: attribute.name,
                                fieldIndex: undefined
                            })
                        }
                        className="rounded-none px-0 py-3 border-b-2 border-surface-container-high"
                    />

                    {displayableErrors.length > 0 && (
                        <span className="mt-1 text-sm text-error" aria-live="polite">
                            {displayableErrors.map(({ errorMessage }, index) => (
                                <span key={`${attribute.name}-err-${index}`}>
                                    {errorMessage}
                                    {index < displayableErrors.length - 1 ? <br /> : null}
                                </span>
                            ))}
                        </span>
                    )}
                </div>
            );
        },
        []
    );

    const [isFormSubmittable, setIsFormSubmittable] = useState(false);
    const [areTermsAccepted, setAreTermsAccepted] = useState(false);

    useLayoutEffect(() => {
        (window as any)["onSubmitRecaptcha"] = () => {
            // @ts-expect-error
            document.getElementById("kc-register-form").requestSubmit();
        };

        return () => {
            delete (window as any)["onSubmitRecaptcha"];
        };
    }, []);

    return (
        <TemplateComponent
            kcContext={kcContext}
            i18n={i18n}
            doUseDefaultCss={doUseDefaultCss}
            classes={classes}
            headerNode={null}
            displayMessage={false}
            displayRequiredFields={false}
        >
            <main className="min-h-screen bg-surface-container-low px-4 py-10 md:py-16 flex items-center justify-center">
                <section className="w-full max-w-md bg-surface-container-lowest rounded-2xl p-8 shadow-2xl shadow-on-surface/10">
                    <header className="mb-8">
                        <div className="flex items-center gap-3 mb-6">
                            <img src={icon} alt="micro-futár logó" className="w-8 h-8" />
                            <span className="text-xl font-bold text-on-surface">micro-futár</span>
                        </div>
                        <h1 className="font-headline text-3xl font-extrabold text-on-surface">
                            {messageHeader !== undefined ? messageHeader : msgStr("registerTitle")}
                        </h1>
                    </header>

                    {messagesPerField.exists("global") && (
                        <p
                            className="mb-4 text-sm text-error"
                            dangerouslySetInnerHTML={{ __html: kcSanitize(messagesPerField.get("global")) }}
                        />
                    )}

                    <form id="kc-register-form" className={kcClsx("kcFormClass")} action={url.registrationAction} method="post">
                        <UserProfileFormFieldsComponent
                            kcContext={kcContext}
                            i18n={i18n}
                            kcClsx={kcClsx as any}
                            onIsFormSubmittableValueChange={setIsFormSubmittable}
                            doMakeUserConfirmPassword={doMakeUserConfirmPassword}
                            BeforeField={renderPrecisionField}
                        />

                        {termsAcceptanceRequired && (
                            <TermsAcceptance
                                i18n={i18n}
                                kcClsx={kcClsx as any}
                                messagesPerField={messagesPerField}
                                areTermsAccepted={areTermsAccepted}
                                onAreTermsAcceptedValueChange={setAreTermsAccepted}
                            />
                        )}

                        {recaptchaRequired && (recaptchaVisible || recaptchaAction === undefined) && (
                            <div className="pt-2">
                                <div className="g-recaptcha" data-size="compact" data-sitekey={recaptchaSiteKey} data-action={recaptchaAction}></div>
                            </div>
                        )}

                        <div className="space-y-4 pt-2">
                            <div id="kc-form-options" className={kcClsx("kcFormOptionsClass")}>
                                <div className={kcClsx("kcFormOptionsWrapperClass")}>
                                    <span>
                                        <a href={url.loginUrl} className="text-sm font-semibold text-on-primary-container hover:underline">
                                            {msg("backToLogin")}
                                        </a>
                                    </span>
                                </div>
                            </div>

                            {recaptchaRequired && !recaptchaVisible && recaptchaAction !== undefined ? (
                                <div id="kc-form-buttons" className={kcClsx("kcFormButtonsClass")}>
                                    <button
                                        className="kinetic-gradient w-full rounded-xl px-6 py-3 text-sm font-bold text-on-primary transition-opacity hover:opacity-90 disabled:opacity-60 g-recaptcha"
                                        data-sitekey={recaptchaSiteKey}
                                        data-callback="onSubmitRecaptcha"
                                        data-action={recaptchaAction}
                                        type="submit"
                                        disabled={!isFormSubmittable || (termsAcceptanceRequired && !areTermsAccepted)}
                                    >
                                        {msg("doRegister")}
                                    </button>
                                </div>
                            ) : (
                                <div id="kc-form-buttons" className={kcClsx("kcFormButtonsClass")}>
                                    <input
                                        disabled={!isFormSubmittable || (termsAcceptanceRequired && !areTermsAccepted)}
                                        className="kinetic-gradient text-on-primary w-full rounded-xl px-6 py-3 text-sm font-bold transition-opacity hover:opacity-90 disabled:opacity-60"
                                        type="submit"
                                        value={msgStr("doRegister")}
                                    />
                                </div>
                            )}
                        </div>
                    </form>
                </section>
            </main>
        </TemplateComponent>
    );
}

function TermsAcceptance(props: {
    i18n: I18n;
    kcClsx: (...classKeys: Array<string | false | null | undefined>) => string;
    messagesPerField: Pick<KcContext["messagesPerField"], "existsError" | "get">;
    areTermsAccepted: boolean;
    onAreTermsAcceptedValueChange: (areTermsAccepted: boolean) => void;
}) {
    const { i18n, kcClsx, messagesPerField, areTermsAccepted, onAreTermsAcceptedValueChange } = props;

    const { msg } = i18n;

    return (
        <>
            <div className="space-y-2 rounded-xl bg-surface-container-low px-4 py-3">
                <p className="text-sm font-semibold text-on-surface">{msg("termsTitle")}</p>
                <div id="kc-registration-terms-text" className="text-sm text-on-surface-variant">
                    {msg("termsText")}
                </div>
            </div>
            <div className="space-y-2">
                <div className={kcClsx("kcLabelWrapperClass") + " flex items-center gap-2"}>
                    <input
                        type="checkbox"
                        id="termsAccepted"
                        name="termsAccepted"
                        className="rounded border-outline-variant text-surface-tint focus:ring-surface-tint"
                        checked={areTermsAccepted}
                        onChange={e => onAreTermsAcceptedValueChange(e.target.checked)}
                        aria-invalid={messagesPerField.existsError("termsAccepted")}
                    />
                    <label htmlFor="termsAccepted" className="text-sm font-medium text-on-surface">
                        {msg("acceptTerms")}
                    </label>
                </div>
                {messagesPerField.existsError("termsAccepted") && (
                    <div>
                        <span
                            id="input-error-terms-accepted"
                            className="text-sm text-error"
                            aria-live="polite"
                            dangerouslySetInnerHTML={{
                                __html: kcSanitize(messagesPerField.get("termsAccepted"))
                            }}
                        />
                    </div>
                )}
            </div>
        </>
    );
}
