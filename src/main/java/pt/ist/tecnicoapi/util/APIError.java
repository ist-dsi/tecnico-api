package pt.ist.tecnicoapi.util;

import com.google.gson.JsonObject;
import org.fenixedu.bennu.TecnicoAPIConfiguration;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import org.springframework.http.HttpStatus;

import java.util.ResourceBundle;

public class APIError extends Error {
    private final HttpStatus status;
    private final String[] args;
    private final Throwable cause;
    private LocalizedString description;
    private String bundle = TecnicoAPIConfiguration.BUNDLE;

    public APIError(final @NotNull HttpStatus status,
                    final @NotNull
                    @PropertyKey(resourceBundle = TecnicoAPIConfiguration.BUNDLE) String message,
                    final @NotNull String... args) {
        super(message);
        this.status = status;
        this.args = args;
        this.cause = null;
    }

    public APIError(final @NotNull String bundle,
                    final @NotNull HttpStatus status,
                    final @NotNull String message,
                    final @NotNull String... args) {
        this(status, message, args);
        this.bundle = bundle;
    }

    public APIError(final @NotNull Throwable cause,
                    final @NotNull HttpStatus status,
                    final @PropertyKey(resourceBundle = TecnicoAPIConfiguration.BUNDLE) String message,
                    final @NotNull String... args) {
        super(message);
        this.status = status;
        this.args = args;
        this.cause = cause;
    }

    public APIError(final @NotNull HttpStatus status,
                    final @NotNull
                    @PropertyKey(resourceBundle = TecnicoAPIConfiguration.BUNDLE) String message,
                    final @NotNull LocalizedString description,
                    final @NotNull String... args) {
        this(status, message, args);
        this.description = description;
    }

    public @NotNull HttpStatus getStatus() {
        return status;
    }

    public @NotNull JsonObject toResponseBody() {
        return JsonUtils.toJson(data -> {
            data.addProperty("key", getMessage());
            if (description != null) {
                data.add("message", description.json());
            } else if (CoreConfiguration.supportedLocales()
                    .stream()
                    .allMatch(locale -> ResourceBundle.getBundle(this.bundle, locale).containsKey(getMessage()))
            ) {
                data.add("message", BundleUtil.getLocalizedString(this.bundle, getMessage(), args).json());
            }
        });
    }

}
