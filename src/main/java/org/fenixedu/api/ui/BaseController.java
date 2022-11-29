package org.fenixedu.api.ui;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.Evaluation;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Grouping;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.degreeStructure.Context;
import org.fenixedu.academic.domain.degreeStructure.CourseGroup;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.util.icalendar.EventBean;
import org.fenixedu.api.oauth.OAuthAuthorizationProvider;
import org.fenixedu.api.serializer.FenixEduAPISerializer;
import org.fenixedu.api.util.APIError;
import org.fenixedu.api.util.APIScope;
import org.fenixedu.bennu.FenixEduAPIConfiguration;
import org.fenixedu.spaces.domain.Space;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pt.ist.fenixframework.DomainObject;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BaseController extends org.fenixedu.bennu.spring.BaseController {

    final static Integer MAX_INTERVAL_DAYS = 30;

    @ExceptionHandler({APIError.class})
    public ResponseEntity<?> handleApiError(final APIError error) {
        return respond(error.getStatus(), error.toResponseBody());
    }

    /**
     * Send a JSON error if a String cannot be converted to a DomainObject
     * instead of Spring's default HTML error
     */
    @ExceptionHandler({ConversionFailedException.class})
    public ResponseEntity<?> handleConversionException(final ConversionFailedException exception) {
        final Class<?> sourceType = exception.getSourceType().getType();
        final Class<?> targetType = exception.getTargetType().getType();
        if (String.class.equals(sourceType) && DomainObject.class.isAssignableFrom(targetType)) {
            final String value = Objects.toString(exception.getValue());
            return handleApiError(
                    new APIError(
                            HttpStatus.NOT_FOUND,
                            "error." + targetType.getSimpleName().toLowerCase() + ".not.found",
                            value
                    )
            );
        }
        throw exception;
    }

    /**
     * This parameter should be added to the route method
     * <code>@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken</code>
     * in order to get the access token.
     *
     * @see OAuthAuthorizationProvider#requireOAuthScope(String, String)
     */

    protected void requireOAuthScope(@Nullable String accessToken, @NotNull APIScope scope) {
        OAuthAuthorizationProvider.requireOAuthScope(accessToken, scope.toString());
    }

    /**
     * Parse a DateTime from a given date. Throws an APIError if it does not have correct format.
     *
     * @param date The given date
     * @return The parsed DateTime
     * @throws APIError if the day does not exist or has invalid formatting
     */
    protected @NotNull DateTime parseDateTimeOrThrow(@NotNull String date) {
        try {
            return DateTime.parse(date);
        } catch (IllegalArgumentException e) {
            throw new APIError(HttpStatus.BAD_REQUEST, "error.datetime.invalid", date);
        }
    }

    /**
     * Parse an Interval from given start and end dates. Throws an APIError if it's an invalid/too long Interval.
     *
     * @param startDate The start date for the interval
     * @param endDate   The end date for the interval
     * @return The parsed Interval
     * @throws APIError if the interval is invalid/too long
     */
    protected @NotNull Interval parseIntervalOrThrow(@Nullable DateTime startDate, @Nullable DateTime endDate) {
        try {
            final Interval interval = new Interval(startDate, endDate);
            final long duration = interval.toDuration().getStandardDays();
            if (duration > MAX_INTERVAL_DAYS) {
                throw new APIError(
                        HttpStatus.BAD_REQUEST,
                        "error.interval.too.long",
                        String.valueOf(duration),
                        MAX_INTERVAL_DAYS.toString()
                );
            }
            return new Interval(startDate, endDate);
        } catch (IllegalArgumentException e) {
            throw new APIError(
                    HttpStatus.BAD_REQUEST,
                    "error.interval.invalid",
                    Objects.toString(startDate),
                    Objects.toString(endDate)
            );
        }
    }

    /**
     * Parse an execution year from its name. Throws an APIError if it does not exist.
     *
     * @param year The name of the execution year
     * @return The parsed execution year
     * @throws APIError if the year does not exist or has invalid formatting
     */
    protected @NotNull ExecutionYear parseExecutionYearOrThrow(@NotNull String year) {
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
     * @param year     The year to parse, in format "2022/2023"
     * @param semester The semester to parse
     * @return A set of execution semesters corresponding to the given input
     * @throws APIError if either the year or the semester does not exist or has invalid formatting
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected @NotNull Set<ExecutionSemester> parseExecutionSemestersOrThrow(@NotNull Optional<String> year,
                                                                             @NotNull Optional<Integer> semester) {
        ExecutionYear executionYear = year.map(this::parseExecutionYearOrThrow)
                .orElseGet(ExecutionYear::readCurrentExecutionYear);
        Set<ExecutionSemester> semesters = executionYear.getExecutionPeriodsSet()
                .stream()
                .filter(
                        executionSemester -> semester
                                .map(semesterNumber -> Objects.equals(executionSemester.getSemester(), semesterNumber))
                                .orElse(year.isPresent() || executionSemester.isCurrent())
                )
                .collect(Collectors.toSet());

        if (semesters.isEmpty()) {
            throw new APIError(
                    HttpStatus.BAD_REQUEST,
                    "error.academicterm.semester.incorrect",
                    executionYear.getName(),
                    semester.map(Objects::toString).orElse("none")
            );
        }

        return semesters;
    }

    /**
     * Shorthand to get the Domain Object serializer.
     *
     * @return The instance of {@link FenixEduAPISerializer}.
     */
    protected @NotNull FenixEduAPISerializer getSerializer() {
        return FenixEduAPIConfiguration.getSerializer();
    }

    protected @NotNull JsonObject toUnitJson(@NotNull Unit unit) {
        return getSerializer().getUnitSerializer().serialize(unit);
    }

    protected @NotNull JsonObject toAttendsJson(@NotNull Attends attends) {
        return getSerializer().getAttendsSerializer().serialize(attends);
    }

    protected @NotNull JsonObject toCurricularCourseJson(@NotNull CurricularCourse course,
                                                         @NotNull ExecutionYear executionYear) {
        return getSerializer().getCurricularCourseSerializer().serialize(course, executionYear);
    }

    protected @NotNull JsonObject toCurricularCourseWithCurricularInformationJson(@NotNull Context context,
                                                                                  @NotNull ExecutionYear executionYear,
                                                                                  @NotNull List<CourseGroup> pathTaken) {
        return getSerializer().getCurricularCourseSerializer()
                .serializeWithCurricularInformation(context, executionYear, pathTaken);
    }

    protected @NotNull JsonObject toExtendedDegreeJson(@NotNull Degree degree) {
        return getSerializer().getDegreeSerializer().serializeExtended(degree);
    }

    protected @NotNull JsonObject toExecutionDegreeJson(@NotNull ExecutionDegree executionDegree) {
        return getSerializer().getExecutionDegreeSerializer().serialize(executionDegree);
    }

    protected @NotNull JsonObject toExtendedExecutionDegreeJson(@NotNull ExecutionDegree executionDegree) {
        return getSerializer().getExecutionDegreeSerializer().serializeExtended(executionDegree);
    }

    protected @NotNull JsonObject toExtendedEvaluationJson(@NotNull Evaluation evaluation) {
        return getSerializer().getEvaluationSerializer().serializeExtended(evaluation);
    }

    protected @NotNull JsonObject toExtendedExecutionCourseJson(@NotNull ExecutionCourse course) {
        return getSerializer().getExecutionCourseSerializer().serializeExtended(course);
    }

    protected @NotNull JsonObject toExtendedExecutionSemesterJson(@NotNull ExecutionSemester executionSemester) {
        return getSerializer().getExecutionSemesterSerializer().serializeExtended(executionSemester);
    }

    protected @NotNull JsonObject toExtendedExecutionYearJson(@NotNull ExecutionYear executionYear) {
        return getSerializer().getExecutionYearSerializer().serializeExtended(executionYear);
    }

    protected @NotNull JsonObject toGroupingJson(@NotNull Grouping grouping) {
        return getSerializer().getGroupingSerializer().serialize(grouping);
    }

    protected @NotNull JsonObject toLocaleJson(@NotNull Locale locale) {
        return getSerializer().getLocaleSerializer().serialize(locale);
    }

    protected @NotNull JsonObject toPersonJson(@NotNull Person person) {
        return getSerializer().getPersonSerializer().serialize(person);
    }

    protected @NotNull JsonObject toScheduleJson(@NotNull ExecutionCourse executionCourse) {
        return getSerializer().getScheduleSerializer().serialize(executionCourse);
    }

    protected @NotNull JsonObject toBasicSpaceJson(@NotNull Space space) {
        return getSerializer().getSpaceSerializer().serializeBasic(space);
    }

    protected @NotNull JsonObject toSpaceJson(@NotNull Space space) {
        return getSerializer().getSpaceSerializer().serialize(space);
    }

    protected @NotNull JsonObject toExtendedSpaceJson(@NotNull Space space, @NotNull Interval interval) {
        return getSerializer().getSpaceSerializer().serializeExtended(space, interval);
    }

    protected @NotNull JsonObject toRegistrationWithCurriculumInformationJson(@NotNull Registration registration) {
        return getSerializer().getRegistrationSerializer().serializeWithCurriculumInformation(registration);
    }

    protected @NotNull JsonObject toEnrolmentJson(@NotNull Enrolment enrolment,
                                                  @NotNull ExecutionSemester executionSemester) {
        return getSerializer().getEnrolmentSerializer().serialize(enrolment, executionSemester);
    }

    protected @NotNull JsonObject toProfessorshipJson(@NotNull Professorship professorship) {
        return getSerializer().getProfessorshipSerializer().serialize(professorship);
    }

    protected @NotNull JsonObject toEvaluationWithEnrolmentStateJson(@NotNull Evaluation evaluation,
                                                                     @NotNull ExecutionCourse course,
                                                                     @NotNull Registration registration) {
        return getSerializer().getEvaluationSerializer().serializeWithEnrolmentState(evaluation, course, registration);
    }

    protected @NotNull JsonObject toEventBeanJson(@NotNull EventBean event) {
        return getSerializer().getEventBeanSerializer().serialize(event);
    }

}
