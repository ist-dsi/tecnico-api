package org.fenixedu.api.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.fenixedu.academic.domain.AdHocEvaluation;
import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeInfo;
import org.fenixedu.academic.domain.Evaluation;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Grouping;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.Project;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.academic.domain.StudentGroup;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.WrittenEvaluation;
import org.fenixedu.academic.domain.degreeStructure.BibliographicReferences;
import org.fenixedu.academic.domain.degreeStructure.BibliographicReferences.BibliographicReference;
import org.fenixedu.academic.domain.degreeStructure.BibliographicReferences.BibliographicReferenceType;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.util.EnrolmentGroupPolicyType;
import org.fenixedu.academic.util.EvaluationType;
import org.fenixedu.api.oauth.OAuthAuthorizationProvider;
import org.fenixedu.api.util.APIError;
import org.fenixedu.api.util.APIScope;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.commons.stream.StreamUtils;
import org.fenixedu.spaces.domain.Space;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pt.ist.fenixframework.DomainObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BaseController extends org.fenixedu.bennu.spring.BaseController {

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
                            HttpStatus.BAD_REQUEST,
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

    protected JsonObject toUnitJson(@Nonnull Unit unit) {
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

    protected JsonObject toAttendsJson(@Nonnull Attends attends) {
        final Registration registration = attends.getRegistration();
        final Person person = registration.getPerson();
        final Degree degree = registration.getDegree();
        return JsonUtils.toJson(data -> {
            data.addProperty("username", person.getUsername());
            data.add("degree", toDegreeJson(degree));
        });
    }

    protected JsonObject toBibliographicReferenceJson(@Nonnull BibliographicReference bibliographicReference) {
        return JsonUtils.toJson(data -> {
            addIfAndFormat(data, "authors", bibliographicReference, BibliographicReference::getAuthors);
            addIfAndFormat(data, "title", bibliographicReference, BibliographicReference::getTitle);
            addIfAndFormat(data, "publisherReference", bibliographicReference, BibliographicReference::getReference);
            addIfAndFormat(data, "type", bibliographicReference.getType(), BibliographicReferenceType::getName);
            addIfAndFormat(data, "url", bibliographicReference, BibliographicReference::getUrl);
            addIfAndFormat(data, "year", bibliographicReference, BibliographicReference::getYear);
        });
    }

    protected JsonObject toCurricularCourseJson(@Nonnull CurricularCourse course, ExecutionYear executionYear) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", course.getExternalId());
            data.add("name", course.getNameI18N().json());
            data.addProperty("acronym", course.getAcronym());
            data.addProperty("credits", course.getEctsCredits(null, executionYear));
            data.add(
                    "executionCourses",
                    course.getExecutionCoursesByExecutionYear(executionYear)
                            .stream()
                            .map(this::toExecutionCourseJson)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

    protected JsonObject toDegreeJson(@Nonnull Degree degree) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", degree.getExternalId());
            data.add("name", degree.getNameI18N().json());
            data.addProperty("acronym", degree.getSigla());
            JsonUtils.addIf(data, "url", degree.getSiteUrl());
            addIfAndFormatElement(
                    data,
                    "campi",
                    degree.getCurrentCampus(),
                    campus -> campus.stream()
                            .map(Space::getName)
                            .map(JsonPrimitive::new)
                            .collect(StreamUtils.toJsonArray())
            );
            addIfAndFormatElement(
                    data,
                    "degreeType",
                    degree.getDegreeType().getName(),
                    LocalizedString::json
            );
        });
    }

    protected JsonObject toExtendedDegreeJson(@Nonnull Degree degree) {
        JsonObject data = toDegreeJson(degree);
        addIfAndFormatElement(
                data,
                "academicTerms",
                degree.getExecutionDegrees(),
                degrees -> degrees.stream()
                        // sorted in ascending order
                        .sorted(ExecutionDegree.REVERSE_EXECUTION_DEGREE_COMPARATORY_BY_YEAR)
                        .map(ExecutionDegree::getExecutionYear)
                        .map(this::toExecutionYearJson)
                        .collect(StreamUtils.toJsonArray())
        );

        // below are the fields used in each degree's homepage
        // e.g https://fenix.tecnico.ulisboa.pt/cursos/leic-a/descricao
        final DegreeInfo degreeInfo = degree.getMostRecentDegreeInfo();
        final ExecutionYear executionYear = degreeInfo.getExecutionYear();
        addIfAndFormatElement(
                data,
                "accessRequisites",
                degreeInfo.getAccessRequisites(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "description",
                degreeInfo.getDescription(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "designedFor",
                degreeInfo.getDesignedFor(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "history",
                degreeInfo.getHistory(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "objectives",
                degreeInfo.getObjectives(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "operationalRegime",
                degreeInfo.getOperationalRegime(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "professionalExits",
                degreeInfo.getProfessionalExits(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "tuitionFees",
                degreeInfo.getGratuity(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "coordinators",
                degree.getResponsibleCoordinatorsTeachers(executionYear),
                teachers -> teachers.stream()
                        .map(this::toTeacherJson)
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

    protected @Nonnull JsonObject toEvaluationJson(@Nonnull Evaluation evaluation) {
        return JsonUtils.toJson(eval -> {
            eval.addProperty("id", evaluation.getExternalId());
            eval.addProperty("name", evaluation.getPresentationName());
            eval.add("type", toEvaluationTypeJson(evaluation.getEvaluationType()));
        });
    }

    protected @Nonnull JsonPrimitive toEvaluationTypeJson(@Nonnull EvaluationType evaluationType) {
        switch (evaluationType.getType()) {
            case 1:
                return new JsonPrimitive("EXAM");
            case 2:
                return new JsonPrimitive("FINAL");
            case 3:
                return new JsonPrimitive("ONLINE_TEST");
            case 4:
                return new JsonPrimitive("TEST");
            case 5:
                return new JsonPrimitive("PROJECT");
            case 6:
                return new JsonPrimitive("AD_HOC");
            default:
                return new JsonPrimitive("UNKNOWN");
        }
    }

    protected @Nonnull JsonObject toExtendedEvaluationJson(@Nonnull AdHocEvaluation evaluation) {
        JsonObject data = toEvaluationJson(evaluation);
        data.addProperty("description", evaluation.getDescription());
        return data;
    }

    protected @Nonnull JsonObject toExtendedEvaluationJson(@Nonnull Project project) {
        JsonObject data = toEvaluationJson(project);
        data.add("evaluationPeriod", JsonUtils.toJson(period -> {
            period.addProperty("start", project.getProjectBeginDateTime().toString());
            period.addProperty("end", project.getProjectEndDateTime().toString());
        }));
        return data;
    }

    protected @Nonnull JsonObject toExtendedEvaluationJson(@Nonnull WrittenEvaluation evaluation) {
        JsonObject data = toEvaluationJson(evaluation);
        if (evaluation.getEnrolmentPeriodStart() != null && evaluation.getEnrolmentPeriodEnd() != null) {
            data.add("enrollmentPeriod", JsonUtils.toJson(period -> {
                period.addProperty("currentlyOpen", evaluation.isInEnrolmentPeriod());
                period.addProperty("start", evaluation.getEnrolmentPeriodStart().toString());
                period.addProperty("end", evaluation.getEnrolmentPeriodEnd().toString());
            }));
        }
        data.add("evaluationPeriod", JsonUtils.toJson(period -> {
            period.addProperty("start", evaluation.getBeginningDateTime().toString());
            period.addProperty("end", evaluation.getEndDateTime().toString());
        }));
        // TODO: toSpaceJson will be done when the spaces endpoint is created
        // addIfAndFormatElement(data, "rooms", evaluation.getAssociatedRooms(), this::toSpaceJson);
        return data;
    }

    protected @Nonnull JsonObject toExecutionCourseJson(@Nonnull ExecutionCourse course) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", course.getExternalId());
            data.addProperty("acronym", course.getSigla());
            data.add("name", course.getNameI18N().json());
            data.add("academicTerm", toExtendedExecutionSemesterJson(course.getExecutionPeriod()));
            data.add("courseInformation", JsonUtils.toJson(info -> {
                info.add("urls", JsonUtils.toJson(urls -> {
                    final String url = course.getSiteUrl();
                    urls.addProperty("courseUrl", url);
                    urls.addProperty("rssAnnouncementsUrl", url.concat("/rss/announcement"));
                    urls.addProperty("rssSummariesUrl", url.concat("/rss/summary"));
                }));
            }));
        });
    }

    protected @Nonnull JsonObject toExtendedExecutionCourseJson(@Nonnull ExecutionCourse course) {
        JsonObject data = toExecutionCourseJson(course);
        JsonObject courseInformation = data.getAsJsonObject("courseInformation");
        courseInformation.add(
                "bibliography",
                course.getCompetenceCourses()
                        .stream()
                        .map(CompetenceCourse::getBibliographicReferences)
                        .map(BibliographicReferences::getBibliographicReferencesSortedByOrder)
                        .flatMap(Collection::stream)
                        .map(this::toBibliographicReferenceJson)
                        .collect(StreamUtils.toJsonArray())
        );
        courseInformation.add(
                "degrees",
                course.getDegreesSortedByDegreeName()
                        .stream()
                        .map(this::toDegreeJson)
                        .collect(StreamUtils.toJsonArray())
        );
        // a non-public version of this could probably return the actual list of enrolled students?
        courseInformation.addProperty("enrolledStudents", course.getTotalEnrolmentStudentNumber());
        courseInformation.addProperty("evaluationMethods", course.getLocalizedEvaluationMethodText());
        courseInformation.add(
                "teachers",
                course.getProfessorshipsSet()
                        .stream()
                        .map(Professorship::getTeacher)
                        .map(this::toTeacherJson)
                        .collect(StreamUtils.toJsonArray())
        );
        data.add("courseInformation", courseInformation);
        return data;
    }

    protected @Nonnull JsonObject toExecutionSemesterJson(@Nonnull ExecutionSemester executionSemester) {
        return JsonUtils.toJson(data -> {
            data.addProperty("displayName", executionSemester.getQualifiedName());
            data.addProperty("semester", executionSemester.getSemester());
            data.addProperty("beginDate", executionSemester.getBeginLocalDate().toString());
            data.addProperty("endDate", executionSemester.getEndLocalDate().toString());
        });
    }

    protected @Nonnull JsonObject toExtendedExecutionSemesterJson(@Nonnull ExecutionSemester executionSemester) {
        JsonObject data = toExecutionSemesterJson(executionSemester);
        addIfAndFormatElement(
                data,
                "year",
                executionSemester.getExecutionYear(),
                this::toExecutionYearJson
        );
        return data;
    }

    protected @Nonnull JsonObject toExecutionYearJson(@Nonnull ExecutionYear executionYear) {
        return JsonUtils.toJson(data -> {
            data.addProperty("displayName", executionYear.getQualifiedName());
            data.addProperty("beginYear", executionYear.getBeginCivilYear());
            data.addProperty("endYear", executionYear.getEndCivilYear());
            data.addProperty("beginDate", executionYear.getBeginLocalDate().toString());
            data.addProperty("endDate", executionYear.getEndLocalDate().toString());
        });
    }

    protected @Nonnull JsonObject toExtendedExecutionYearJson(@Nonnull ExecutionYear executionYear) {
        JsonObject data = toExecutionYearJson(executionYear);
        data.add(
                "firstSemester",
                toExecutionSemesterJson(executionYear.getFirstExecutionPeriod())
        );
        data.add(
                "secondSemester",
                toExecutionSemesterJson(executionYear.getLastExecutionPeriod())
        );
        return data;
    }

    protected @Nonnull JsonObject toGroupingJson(@Nonnull Grouping grouping) {
        return JsonUtils.toJson(data -> {
            data.addProperty("name", grouping.getName());
            data.addProperty("description", grouping.getProjectDescription());
            data.add("enrollmentPeriod", JsonUtils.toJson(period -> {
                period.addProperty("start", grouping.getEnrolmentBeginDayDateDateTime().toString());
                period.addProperty("end", grouping.getEnrolmentEndDayDateDateTime().toString());
                addIfAndFormatElement(
                        period,
                        "policy",
                        grouping.getEnrolmentPolicy(),
                        this::toEnrolmentPolicyJson
                );
            }));
            data.add("capacity", JsonUtils.toJson(capacity -> {
                capacity.addProperty("minimum", grouping.getMinimumCapacity());
                capacity.addProperty("maximum", grouping.getMaximumCapacity());
                capacity.addProperty("ideal", grouping.getIdealCapacity());
            }));
            data.add(
                    "courses",
                    grouping.getExecutionCourses()
                            .stream()
                            .sorted(ExecutionCourse.EXECUTION_COURSE_NAME_COMPARATOR)
                            .map(this::toExecutionCourseJson)
                            .collect(StreamUtils.toJsonArray())
            );
            data.add(
                    "groups",
                    grouping.getStudentGroupsSet()
                            .stream()
                            .sorted(StudentGroup.COMPARATOR_BY_GROUP_NUMBER)
                            .map(this::toStudentGroupJson)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

    protected @Nullable JsonPrimitive toEnrolmentPolicyJson(@Nonnull EnrolmentGroupPolicyType enrolmentGroupPolicyType) {
        switch (enrolmentGroupPolicyType.getType()) {
            case 1:
                return new JsonPrimitive("ATOMIC");
            case 2:
                return new JsonPrimitive("INDIVIDUAL");
        }
        return null;
    }

    protected @Nonnull JsonObject toLocaleJson(@Nonnull Locale locale) {
        return JsonUtils.toJson(data -> {
            data.addProperty("locale", locale.toLanguageTag());
            data.addProperty("name", locale.getDisplayName());
        });
    }

    protected @Nonnull JsonObject toRegistrationJson(@Nonnull Registration registration) {
        return JsonUtils.toJson(data -> {
            data.add("degreeName", registration.getDegree().getNameI18N().json());
            data.addProperty("degreeAcronym", registration.getDegree().getSigla());
            data.add("degreeType", registration.getDegree().getDegreeType().getName().json());
            data.addProperty("degreeId", registration.getDegree().getExternalId());
            final JsonArray academicTerms = registration.getEnrolmentsExecutionPeriods()
                    .stream()
                    .sorted(ExecutionSemester.COMPARATOR_BY_SEMESTER_AND_YEAR)
                    .map(this::toExtendedExecutionSemesterJson)
                    .collect(StreamUtils.toJsonArray());
            data.add("academicTerms", academicTerms);
        });
    }

    protected @Nonnull JsonObject toStudentGroupJson(@Nonnull StudentGroup studentGroup) {
        return JsonUtils.toJson(data -> {
            data.addProperty("groupNumber", studentGroup.getGroupNumber());
            addIfAndFormat(data, "shift", studentGroup.getShift(), Shift::getPresentationName);
            data.add(
                    "members",
                    studentGroup.getAttendsSet()
                            .stream()
                            .sorted(Attends.COMPARATOR_BY_STUDENT_NUMBER)
                            .map(this::toAttendsJson)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

    protected @Nonnull JsonObject toTeacherJson(@Nonnull Teacher teacher) {
        Person person = teacher.getPerson();

        return JsonUtils.toJson(data -> {
            data.addProperty("username", teacher.getTeacherId());
            data.addProperty("name", person.getName());
            // FIXME: the old API returned a list of mails - is one enough (same for web addresses)?
            if (person.hasDefaultEmailAddress()) {
                data.addProperty("emailAddress", person.getDefaultEmailAddressValue());
            }
            // clicking on the teacher's name will redirect to the teacher's designated "homepage", hence returning the web address(es)
            if (person.hasDefaultWebAddress()) {
                data.addProperty("webAddress", person.getDefaultWebAddressUrl());
            }
        });
    }

    protected <T> void addIfAndFormat(@Nonnull JsonObject object,
                                      @Nonnull String key,
                                      @Nullable T value,
                                      @Nonnull Function<T, String> format) {
        if (value != null) {
            String formattedValue = format.apply(value);
            if (formattedValue != null) {
                object.addProperty(key, formattedValue);
            }
        }
    }

    protected <T> void addIfAndFormatElement(JsonObject object, String key, T value, Function<T, JsonElement> format) {
        if (value != null) {
            JsonElement formattedValue = format.apply(value);
            if (formattedValue != null) {
                object.add(key, formattedValue);
            }
        }
    }

}
