package org.fenixedu.api.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.api.oauth.OAuthAuthorizationProvider;
import org.fenixedu.api.util.APIError;
import org.fenixedu.api.util.APIScope;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collector;

public class BaseController extends org.fenixedu.bennu.spring.BaseController {

    protected final static Collector<JsonElement, JsonArray, JsonArray> jsonArrayCollector = Collector.of(
            JsonArray::new,
            JsonArray::add,
            (a1, a2) -> JsonUtils.toJsonArray(array -> {
                array.addAll(a1);
                array.addAll(a2);
            })
    );

    @ExceptionHandler({APIError.class})
    public ResponseEntity<?> handleApiError(final APIError error) {
        return ResponseEntity.status(error.getStatus()).body(error.toResponseBody());
    }

    /**
     * This parameter should be added to the route method
     * <code>@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken</code>
     * in order to get the access token.
     *
     * @see OAuthAuthorizationProvider#requireOAuthScope(String, String)
     */
    protected void requireOAuthScope(String accessToken, APIScope scope) {
        OAuthAuthorizationProvider.requireOAuthScope(accessToken, scope.toString());
    }

    protected JsonObject toUnitJson(Unit unit) {
        if (unit == null) {
            return null;
        }
        return JsonUtils.toJson(institution -> {
            institution.add("name", unit.getNameI18n().json());
            institution.addProperty("acronym", unit.getAcronym());
            if (unit.hasDefaultWebAddress()) {
                institution.addProperty("url", unit.getDefaultWebAddressUrl());
            }
            if (unit.hasDefaultEmailAddress()) {
                institution.addProperty("email", unit.getDefaultEmailAddressValue());
            }
            if (unit.hasDefaultMobilePhone()) {
                institution.addProperty("phone", unit.getDefaultMobilePhoneNumber());
            }
        });
    }

    protected JsonObject toExecutionSemesterJson(@Nonnull ExecutionSemester executionSemester, boolean includeYear) {
        return JsonUtils.toJson(semester -> {
            semester.addProperty("displayName", executionSemester.getQualifiedName());
            semester.addProperty("semester", executionSemester.getSemester());
            semester.addProperty("beginDate", executionSemester.getBeginLocalDate().toString());
            semester.addProperty("endDate", executionSemester.getEndLocalDate().toString());
            if (includeYear) {
                addIfAndFormatElement(semester, "year",
                    executionSemester.getExecutionYear(),
                    executionYear -> toExecutionYearJson(executionYear, false)
                );
            }
        });
    }

    protected JsonObject toExecutionYearJson(@Nonnull ExecutionYear executionYear, boolean includeSemesters) {
        return JsonUtils.toJson(year -> {
            year.addProperty("displayName", executionYear.getQualifiedName());
            year.addProperty("beginYear", executionYear.getBeginCivilYear());
            year.addProperty("endYear", executionYear.getEndCivilYear());
            year.addProperty("beginDate", executionYear.getBeginLocalDate().toString());
            year.addProperty("endDate", executionYear.getEndLocalDate().toString());
            
            if (includeSemesters) {
                year.add(
                    "firstSemester",
                    toExecutionSemesterJson(executionYear.getFirstExecutionPeriod(), false)
                );
                year.add(
                    "secondSemester",
                    toExecutionSemesterJson(executionYear.getLastExecutionPeriod(), false)
                );
            }
        });
    }

    protected JsonObject toLocaleJson(Locale locale) {
        return JsonUtils.toJson(localeJson -> {
            localeJson.addProperty("locale", locale.toLanguageTag());
            localeJson.addProperty("name", locale.getDisplayName());
        });
    }

    protected JsonObject toRegistrationJson(Registration registration) {
        return JsonUtils.toJson(data -> {
            data.add("degreeName", registration.getDegree().getNameI18N().json());
            data.addProperty("degreeAcronym", registration.getDegree().getSigla());
            data.add("degreeType", registration.getDegree().getDegreeType().getName().json());
            data.addProperty("degreeId", registration.getDegree().getExternalId());
            final JsonArray academicTerms = registration.getEnrolmentsExecutionPeriods()
                    .stream().map(executionSemester -> this.toExecutionSemesterJson(executionSemester, true))
                    .collect(jsonArrayCollector);
            data.add("academicTerms", academicTerms);
        });
    }

    protected <T> void addIfAndFormat(JsonObject object, String key, T value, Function<T, String> format) {
        if (value != null) {
            object.addProperty(key, format.apply(value));
        }
    }

    protected <T> void addIfAndFormatElement(JsonObject object, String key, T value, Function<T, JsonElement> format) {
        if (value != null) {
            object.add(key, format.apply(value));
        }
    }

}
