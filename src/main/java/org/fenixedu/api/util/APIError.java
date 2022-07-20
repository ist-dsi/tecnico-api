package org.fenixedu.api.util;

import org.fenixedu.bennu.FenixEduAPIConfiguration;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.springframework.http.HttpStatus;

import java.util.ResourceBundle;

public class APIError extends Error {
    private final HttpStatus status;
    private final String[] args;
    private final Throwable cause;
    private LocalizedString description;
    private String bundle = FenixEduAPIConfiguration.BUNDLE;

    public APIError(final HttpStatus status, final String message, final String... args) {
        super(message);
        this.status = status;
        this.args = args;
        this.cause = null;
    }

    public APIError(final String bundle, final HttpStatus status, final String message, final String... args) {
        this(status, message, args);
        this.bundle = bundle;
    }

    public APIError(final Throwable cause, final HttpStatus status, final String message, final String... args) {
        super(message);
        this.status = status;
        this.args = args;
        this.cause = cause;
    }

    public APIError(final HttpStatus status, final String message, final LocalizedString description, final String... args) {
        this(status, message, args);
        this.description = description;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String toResponseBody() {
        return JsonUtils.toJson(data -> {
            data.addProperty("key", getMessage());
            if (description != null) {
                data.add("message", description.json());
            } else if (CoreConfiguration.supportedLocales().stream().allMatch(locale -> ResourceBundle.getBundle(this.bundle, locale).containsKey(getMessage()))) {
                data.add("message", BundleUtil.getLocalizedString(this.bundle, getMessage(), args).json());
            }
        }).toString();
    }

}
