package org.fenixedu.api.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.api.oauth.OAuthAuthorizationProvider;
import org.fenixedu.api.util.APIError;
import org.fenixedu.api.util.APIScope;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

    /**
     * Parse an execution year from its name. Throws an APIError if it does not exist.
     *
     * @param year The name of the execution year
     * @return The parsed execution year
     * @throws APIError if the year does not exist or has invalid formatting
     */
    protected ExecutionYear parseExecutionYearOrThrow(String year) {
        return Optional.ofNullable(ExecutionYear.readExecutionYearByName(year))
                .orElseThrow(() -> new APIError(HttpStatus.BAD_REQUEST, "error.academicterm.year.incorrect", year));
    }

    /**
     * Parse a given combination of year and semester, usually given as query parameters of some endpoints.
     * This will follow the given logic:
     * - if both year and semester are given, it returns only that semester of that year
     * - if only the year is given, it returns both semesters of that year
     * - if only the semester is given, it returns that semester of the current year
     * - if neither the year nor semester are given, it returns the current semester of the current year
     *
     * @param year     The year to parse
     * @param semester The semester to parse
     * @return A set of execution semesters corresponding to the given input
     * @throws APIError if either the year or the semester does not exist or has invalid formatting
     */
    protected Set<ExecutionSemester> parseExecutionSemestersOrThrow(Optional<String> year, Optional<Integer> semester) {
        ExecutionYear executionYear = year.map(this::parseExecutionYearOrThrow).orElseGet(ExecutionYear::readCurrentExecutionYear);
        Set<ExecutionSemester> semesters = executionYear.getExecutionPeriodsSet().stream()
                .filter(executionSemester -> semester
                        .map(semesterNumber -> Objects.equals(executionSemester.getSemester(), semesterNumber))
                        .orElse(year.isPresent() || executionSemester.isCurrent())
                )
                .collect(Collectors.toSet());

        if (semesters.isEmpty()) {
            throw new APIError(HttpStatus.BAD_REQUEST, "error.academicterm.semester.incorrect",
                    executionYear.getName(), semester.map(Objects::toString).orElse("none"));
        }

        return semesters;
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

    protected JsonObject toExecutionCourseJson(ExecutionCourse course) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", course.getExternalId());
            data.addProperty("acronym", course.getSigla());
            data.add("name", course.getNameI18N().json());
            data.add("academicTerm", toExecutionSemesterJson(course.getExecutionPeriod(), true));
            data.addProperty("url", course.getSiteUrl());
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
