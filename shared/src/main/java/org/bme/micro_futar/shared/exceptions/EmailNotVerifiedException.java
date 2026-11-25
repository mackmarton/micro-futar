package org.bme.micro_futar.shared.exceptions;

/** The caller is logged in but has no verified email to be identified by. Answered with 403 and {@link #CODE}. */
public class EmailNotVerifiedException extends RuntimeException {

    public static final String CODE = "EMAIL_NOT_VERIFIED";

    public EmailNotVerifiedException() {
        super("Email address is not verified");
    }
}
