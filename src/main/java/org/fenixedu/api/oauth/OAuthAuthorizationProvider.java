package org.fenixedu.api.oauth;

import com.google.common.base.Strings;
import org.fenixedu.api.util.APIError;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.oauth.domain.ApplicationUserSession;
import org.fenixedu.bennu.oauth.domain.ExternalApplication;
import org.fenixedu.bennu.oauth.domain.ExternalApplicationScope;
import org.fenixedu.bennu.oauth.domain.ServiceApplication;
import org.fenixedu.bennu.oauth.util.OAuthUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class OAuthAuthorizationProvider {

    /**
     * Assert that the access token belongs to an application and has the required OAuth scope.
     * Additionally, mocks authentication as the related user.
     *
     * @param accessToken The value of the Authorization HTTP header.
     * @param scopeKey    The key of the scope.
     * @throws APIError if the token is invalid, the app does not have the required scope, or the token has expired.
     */
    public static void requireOAuthScope(@Nullable String accessToken, @NotNull String scopeKey) {
        accessToken = parseAccessToken(accessToken);

        Optional<ExternalApplicationScope> scope = ExternalApplicationScope.forKey(scopeKey);

        if (!scope.isPresent()) {
            throw new APIError(HttpStatus.INTERNAL_SERVER_ERROR, "error.oauth.scopes.unknown");
        }

        Optional<ApplicationUserSession> session = extractUserSession(accessToken);
        if (!session.isPresent()) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "error.oauth.accessToken.invalid");
        }

        ApplicationUserSession appUserSession = session.get();
        ExternalApplication app = appUserSession.getApplicationUserAuthorization().getApplication();

        if (app.isDeleted()) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "error.oauth.accessToken.invalid");
        }

        if (app.isBanned()) {
            throw new APIError(HttpStatus.FORBIDDEN, "error.oauth.app.banned");
        }

        if (!app.getScopesSet().contains(scope.get())) {
            throw new APIError(HttpStatus.FORBIDDEN, "error.oauth.scopes.insufficient", scope.get().getScopeKey());
        }

        if (!appUserSession.matchesAccessToken(accessToken)) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "error.oauth.accessToken.invalid");
        }

        if (!appUserSession.isAccessTokenValid()) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "error.oauth.accessToken.expired");
        }

        User foundUser = appUserSession.getApplicationUserAuthorization().getUser();

        if (foundUser.isLoginExpired()) {
            throw new APIError(HttpStatus.UNAUTHORIZED, "error.oauth.accessToken.expired");
        }

        Authenticate.mock(foundUser, "OAuth Access Token");

    }

    public static void requireServiceAccount() {
        // TODO
    }

    private static @NotNull Optional<String> extractAccessToken(@Nullable String accessToken) {
        if (Strings.isNullOrEmpty(accessToken)) {
            return Optional.empty();
        }

        try {
            String fullToken = new String(Base64.getDecoder().decode(accessToken), StandardCharsets.UTF_8);
            String[] accessTokenBuilder = fullToken.split(":");
            if (accessTokenBuilder.length != 2) {
                return Optional.empty();
            }
            return Optional.of(accessTokenBuilder[0]);
        } catch (IllegalArgumentException iea) {
            return Optional.empty();
        }
    }

    private static @NotNull Optional<ServiceApplication> extractServiceApplication(@Nullable String accessToken) {
        Optional<String> objectId = extractAccessToken(accessToken);
        if (!objectId.isPresent()) {
            return Optional.empty();
        }
        return OAuthUtils.getDomainObject(objectId.get(), ServiceApplication.class);
    }

    private static @NotNull Optional<ApplicationUserSession> extractUserSession(@Nullable String accessToken) {
        Optional<String> objectId = extractAccessToken(accessToken);
        if (!objectId.isPresent()) {
            return Optional.empty();
        }
        return OAuthUtils.getDomainObject(objectId.get(), ApplicationUserSession.class);
    }

    @Contract("null -> null")
    private static @Nullable String parseAccessToken(@Nullable String accessToken) {
        if (accessToken != null && accessToken.startsWith(OAuthUtils.TOKEN_TYPE_HEADER_ACCESS_TOKEN)) {
            return accessToken.substring(OAuthUtils.TOKEN_TYPE_HEADER_ACCESS_TOKEN.length()).trim();
        }
        return accessToken;
    }

}
