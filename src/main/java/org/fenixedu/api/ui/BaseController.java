package org.fenixedu.api.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.fenixedu.academic.domain.AdHocEvaluation;
import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.Coordinator;
import org.fenixedu.academic.domain.CourseLoad;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeInfo;
import org.fenixedu.academic.domain.Evaluation;
import org.fenixedu.academic.domain.Exam;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Grouping;
import org.fenixedu.academic.domain.Lesson;
import org.fenixedu.academic.domain.LessonInstance;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.Project;
import org.fenixedu.academic.domain.SchoolClass;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.academic.domain.StudentGroup;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.WrittenEvaluation;
import org.fenixedu.academic.domain.curricularPeriod.CurricularPeriod;
import org.fenixedu.academic.domain.degreeStructure.BibliographicReferences;
import org.fenixedu.academic.domain.degreeStructure.BibliographicReferences.BibliographicReference;
import org.fenixedu.academic.domain.degreeStructure.BibliographicReferences.BibliographicReferenceType;
import org.fenixedu.academic.domain.degreeStructure.Context;
import org.fenixedu.academic.domain.degreeStructure.CourseGroup;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.space.EventSpaceOccupation;
import org.fenixedu.academic.domain.space.LessonInstanceSpaceOccupation;
import org.fenixedu.academic.domain.space.LessonSpaceOccupation;
import org.fenixedu.academic.domain.space.SpaceUtils;
import org.fenixedu.academic.domain.space.WrittenEvaluationSpaceOccupation;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.util.EnrolmentGroupPolicyType;
import org.fenixedu.academic.util.EvaluationType;
import org.fenixedu.api.oauth.OAuthAuthorizationProvider;
import org.fenixedu.api.util.APIError;
import org.fenixedu.api.util.APIScope;
import org.fenixedu.api.util.SpaceType;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.commons.stream.StreamUtils;
import org.fenixedu.spaces.domain.Space;
import org.fenixedu.spaces.domain.occupation.Occupation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pt.ist.fenixframework.DomainObject;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    protected DateTime parseDateTimeOrThrow(String date) {
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
    protected Interval parseIntervalOrThrow(DateTime startDate, DateTime endDate) {
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
                    startDate.toString(),
                    endDate.toString()
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

    protected @NotNull JsonObject toUnitJson(@NotNull Unit unit) {
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

    protected @NotNull JsonObject toAttendsJson(@NotNull Attends attends) {
        final Registration registration = attends.getRegistration();
        final Person person = registration.getPerson();
        final Degree degree = registration.getDegree();
        return JsonUtils.toJson(data -> {
            data.addProperty("username", person.getUsername());
            data.add("degree", toDegreeJson(degree));
        });
    }

    protected @NotNull JsonObject toBibliographicReferenceJson(@NotNull BibliographicReference bibliographicReference) {
        return JsonUtils.toJson(data -> {
            addIfAndFormat(data, "authors", bibliographicReference, BibliographicReference::getAuthors);
            addIfAndFormat(data, "title", bibliographicReference, BibliographicReference::getTitle);
            addIfAndFormat(data, "publisherReference", bibliographicReference, BibliographicReference::getReference);
            addIfAndFormat(data, "type", bibliographicReference.getType(), BibliographicReferenceType::getName);
            addIfAndFormat(data, "url", bibliographicReference, BibliographicReference::getUrl);
            addIfAndFormat(data, "year", bibliographicReference, BibliographicReference::getYear);
        });
    }

    protected @NotNull JsonObject toCurricularCourseJson(@NotNull CurricularCourse course,
                                                         @NotNull ExecutionYear executionYear) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", course.getExternalId());
            data.add("name", course.getNameI18N().json());
            data.addProperty("acronym", course.getAcronym());
            data.addProperty("credits", course.getEctsCredits(executionYear));
            data.add(
                    "executions",
                    course.getExecutionCoursesByExecutionYear(executionYear)
                            .stream()
                            .map(this::toExecutionCourseJson)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

    // TODO add javadocs saying context must contain a curricular course

    /**
     * Serialize a {@link Context} that wraps a {@link CurricularCourse}.
     *
     * @param context       A {@link Context} that wraps a {@link CurricularCourse}. If it does not wrap a course, it
     *                      will throw an error!
     * @param executionYear The year to fetch course data from.
     * @param pathTaken     The path that has been taken until the current course group (inclusive).
     * @return Serialized data
     */
    protected @NotNull JsonObject toCurricularCourseWithCurricularInformationJson(@NotNull Context context,
                                                                                  @NotNull ExecutionYear executionYear,
                                                                                  @NotNull List<CourseGroup> pathTaken) {
        final CurricularCourse course = (CurricularCourse) context.getChildDegreeModule();
        final CurricularPeriod period = context.getCurricularPeriod();
        // "term" is the half of the semester when the course occurs, or null if it's the whole semester
        final Integer term = context.getTerm();

        final JsonObject data = toCurricularCourseJson(course, executionYear);
        data.add("workload", JsonUtils.toJson(workload -> {
            workload.addProperty("autonomous", course.getAutonomousWorkHours());
            workload.addProperty("contact", course.getContactLoad());
            workload.addProperty("total", course.getTotalLoad());
        }));
        addIfAndFormatElement(data, "curricularPeriod", period, p -> toCurricularPeriodJson(p, term));
        data.addProperty("executionInterval", course.getRegime().getLocalizedName());
        data.addProperty("optional", course.isOptional());
        data.add(
                "courseGroups",
                pathTaken
                        .stream()
                        .map(this::toCourseGroupJson)
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

    protected @NotNull JsonObject toCurricularPeriodJson(@NotNull final CurricularPeriod curricularPeriod,
                                                         @Nullable final Integer term) {
        return JsonUtils.toJson(data -> {
            // "term" is the half of the semester when the course occurs, or null if it's the whole semester
            addIfAndFormatElement(data, "quarter", term, JsonPrimitive::new);

            CurricularPeriod currentPeriod = curricularPeriod;
            do {
                addIfAndFormatElement(
                        data,
                        currentPeriod.getAcademicPeriod().getName().toLowerCase(Locale.ROOT),
                        currentPeriod.getChildOrder(),
                        JsonPrimitive::new
                );
                currentPeriod = currentPeriod.getParent();
            } while (currentPeriod != null);
        });
    }

    protected @NotNull JsonObject toCourseGroupJson(@NotNull CourseGroup courseGroup) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", courseGroup.getExternalId());
            data.add("name", courseGroup.getNameI18N().json());
        });
    }

    protected @NotNull JsonObject toDegreeJson(@NotNull Degree degree) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", degree.getExternalId()); // avoid exposing executionDegree complexity
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

    protected @NotNull JsonObject toExtendedDegreeJson(@NotNull Degree degree) {
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
        return data;
    }

    protected @NotNull JsonObject toExecutionDegreeJson(@NotNull ExecutionDegree executionDegree) {
        return toExtendedDegreeJson(executionDegree.getDegree());
    }

    protected @NotNull JsonObject toExtendedExecutionDegreeJson(@NotNull ExecutionDegree executionDegree) {
        final Degree degree = executionDegree.getDegree();
        final ExecutionYear executionYear = executionDegree.getExecutionYear();
        final DegreeInfo degreeInfo = degree.getMostRecentDegreeInfo(executionYear.getAcademicInterval());
        final JsonObject data = toExtendedDegreeJson(degree);
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
                executionDegree.getResponsibleCoordinators(),
                teachers -> teachers.stream()
                        .map(Coordinator::getTeacher)
                        .map(this::toTeacherJson)
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

    protected @NotNull JsonObject toEvaluationJson(@NotNull Evaluation evaluation) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", evaluation.getExternalId());
            data.addProperty("name", evaluation.getPresentationName());
            data.add("type", toEvaluationTypeJson(evaluation.getEvaluationType()));
            data.add(
                    "courses",
                    evaluation.getAssociatedExecutionCoursesSet()
                            .stream()
                            .map(this::toExecutionCourseJson)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

    protected @NotNull JsonPrimitive toEvaluationTypeJson(@NotNull EvaluationType evaluationType) {
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

    protected @NotNull JsonObject toExtendedEvaluationJson(@NotNull Evaluation evaluation) {
        if (evaluation instanceof AdHocEvaluation) {
            return toExtendedEvaluationJson((AdHocEvaluation) evaluation);
        } else if (evaluation instanceof Project) {
            return toExtendedEvaluationJson((Project) evaluation);
        } else if (evaluation instanceof WrittenEvaluation) {
            return toExtendedEvaluationJson((WrittenEvaluation) evaluation);
        }
        return toEvaluationJson(evaluation);
    }

    protected @NotNull JsonObject toExtendedEvaluationJson(@NotNull AdHocEvaluation evaluation) {
        JsonObject data = toEvaluationJson(evaluation);
        data.addProperty("description", evaluation.getDescription());
        return data;
    }

    protected @NotNull JsonObject toExtendedEvaluationJson(@NotNull Project project) {
        JsonObject data = toEvaluationJson(project);
        data.add("evaluationPeriod", JsonUtils.toJson(period -> {
            period.addProperty("start", project.getProjectBeginDateTime().toString());
            period.addProperty("end", project.getProjectEndDateTime().toString());
        }));
        return data;
    }

    protected @NotNull JsonObject toExtendedEvaluationJson(@NotNull WrittenEvaluation evaluation) {
        JsonObject data = toEvaluationJson(evaluation);
        if (evaluation.isExam()) {
            data.addProperty("season", ((Exam) evaluation).getSeason().toString());
        }
        if (evaluation.getEnrolmentPeriodStart() != null && evaluation.getEnrolmentPeriodEnd() != null) {
            data.add("enrolmentPeriod", JsonUtils.toJson(period -> {
                period.addProperty("currentlyOpen", evaluation.isInEnrolmentPeriod());
                period.addProperty("start", evaluation.getEnrolmentPeriodStart().toString());
                period.addProperty("end", evaluation.getEnrolmentPeriodEnd().toString());
            }));
        }
        data.add("evaluationPeriod", JsonUtils.toJson(period -> {
            period.addProperty("start", evaluation.getBeginningDateTime().toString());
            period.addProperty("end", evaluation.getEndDateTime().toString());
        }));
        addIfAndFormatElement(
                data,
                "rooms",
                evaluation.getAssociatedRooms(),
                rooms -> rooms.stream().map(this::toSpaceJson).collect(StreamUtils.toJsonArray())
        );
        return data;
    }

    protected @NotNull JsonObject toExecutionCourseJson(@NotNull ExecutionCourse course) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", course.getExternalId());
            data.addProperty("acronym", course.getSigla());
            data.add("name", course.getNameI18N().json());
            data.add("academicTerm", toExtendedExecutionSemesterJson(course.getExecutionPeriod()));
            data.add("courseInformation", JsonUtils.toJson(info -> info.add("urls", JsonUtils.toJson(urls -> {
                final String url = course.getSiteUrl();
                urls.addProperty("courseUrl", url);
                urls.addProperty("rssAnnouncementsUrl", url.concat("/rss/announcement"));
                urls.addProperty("rssSummariesUrl", url.concat("/rss/summary"));
            }))));
        });
    }

    protected @NotNull JsonObject toExtendedExecutionCourseJson(@NotNull ExecutionCourse course) {
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
                course.getExecutionDegrees()
                        .stream()
                        .sorted(ExecutionDegree.COMPARATOR_BY_DEGREE_NAME)
                        .map(ExecutionDegree::getDegree)
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

    protected @NotNull JsonObject toExecutionSemesterJson(@NotNull ExecutionSemester executionSemester) {
        return JsonUtils.toJson(data -> {
            data.addProperty("displayName", executionSemester.getQualifiedName());
            data.addProperty("semester", executionSemester.getSemester());
            data.addProperty("beginDate", executionSemester.getBeginLocalDate().toString());
            data.addProperty("endDate", executionSemester.getEndLocalDate().toString());
        });
    }

    protected @NotNull JsonObject toExtendedExecutionSemesterJson(@NotNull ExecutionSemester executionSemester) {
        JsonObject data = toExecutionSemesterJson(executionSemester);
        addIfAndFormatElement(
                data,
                "year",
                executionSemester.getExecutionYear(),
                this::toExecutionYearJson
        );
        return data;
    }

    protected @NotNull JsonObject toExecutionYearJson(@NotNull ExecutionYear executionYear) {
        return JsonUtils.toJson(data -> {
            data.addProperty("displayName", executionYear.getQualifiedName());
            data.addProperty("beginYear", executionYear.getBeginCivilYear());
            data.addProperty("endYear", executionYear.getEndCivilYear());
            data.addProperty("beginDate", executionYear.getBeginLocalDate().toString());
            data.addProperty("endDate", executionYear.getEndLocalDate().toString());
        });
    }

    protected @NotNull JsonObject toExtendedExecutionYearJson(@NotNull ExecutionYear executionYear) {
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

    protected @NotNull JsonObject toGroupingJson(@NotNull Grouping grouping) {
        return JsonUtils.toJson(data -> {
            data.addProperty("name", grouping.getName());
            data.addProperty("description", grouping.getProjectDescription());
            data.add("enrolmentPeriod", JsonUtils.toJson(period -> {
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

    protected @NotNull JsonObject toLessonWithIntervalJson(@NotNull Lesson lesson, @NotNull Interval interval) {
        return JsonUtils.toJson(data -> {
            data.addProperty("start", interval.getStart().toString());
            data.addProperty("end", interval.getEnd().toString());
            data.add("room", toBasicSpaceJson(lesson.getSala()));
        });
    }

    protected @NotNull JsonObject toLessonInstanceJson(@NotNull LessonInstance lessonInstance) {
        return JsonUtils.toJson(data -> {
            data.addProperty("start", lessonInstance.getBeginDateTime().toString());
            data.addProperty("end", lessonInstance.getEndDateTime().toString());
            data.add("room", toBasicSpaceJson(lessonInstance.getRoom()));
        });
    }

    protected @Nullable JsonPrimitive toEnrolmentPolicyJson(@NotNull EnrolmentGroupPolicyType enrolmentGroupPolicyType) {
        switch (enrolmentGroupPolicyType.getType()) {
            case 1:
                return new JsonPrimitive("ATOMIC");
            case 2:
                return new JsonPrimitive("INDIVIDUAL");
        }
        return null;
    }

    protected @NotNull JsonObject toLocaleJson(@NotNull Locale locale) {
        return JsonUtils.toJson(data -> {
            data.addProperty("locale", locale.toLanguageTag());
            data.addProperty("name", locale.getDisplayName());
        });
    }

    protected @NotNull JsonObject toRegistrationJson(@NotNull Registration registration) {
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

    protected @NotNull JsonObject toScheduleJson(@NotNull ExecutionCourse executionCourse) {
        return JsonUtils.toJson(data -> {
            // data.add(
            //         "lessonPeriods",
            //         executionCourse.getLessonPeriods()
            //                 .stream()
            //                 .map(OccupationPeriod::getIntervals)
            //                 .flatMap(Collection::stream)
            //                 .map(this::toIntervalJson)
            //                 .collect(StreamUtils.toJsonArray())
            // ); FIXME: This is not working - issue already found in V1
            data.add(
                    "courseLoads",
                    executionCourse.getCourseLoadsSet()
                            .stream()
                            .map(this::toCourseLoadJson)
                            .collect(StreamUtils.toJsonArray())
            );
            data.add(
                    "shifts",
                    executionCourse.getAssociatedShifts()
                            .stream()
                            .sorted(Shift.SHIFT_COMPARATOR_BY_NAME)
                            .map(this::toExtendedShiftJson)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

    protected @NotNull JsonObject toCourseLoadJson(CourseLoad courseLoad) {
        return JsonUtils.toJson(data -> {
            data.addProperty("type", courseLoad.getType().name());
            data.addProperty("weeklyHours", courseLoad.getWeeklyHours());
            data.addProperty("totalHours", courseLoad.getTotalQuantity());
        });
    }

    protected @NotNull JsonObject toShiftJson(Shift shift) {
        return JsonUtils.toJson(data -> {
            data.addProperty("name", shift.getNome());
            data.add(
                    "types",
                    shift.getTypes()
                            .stream()
                            .map(type -> new JsonPrimitive(type.getName()))
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

    protected @NotNull JsonObject toExtendedShiftJson(Shift shift) {
        JsonObject data = toShiftJson(shift);
        data.add("enrolments", JsonUtils.toJson(capacity -> {
            capacity.addProperty("maximum", shift.getLotacao());
            capacity.addProperty("current", shift.getStudentsSet().size());
        }));
        data.add(
                "classes",
                shift.getAssociatedClassesSet()
                        .stream()
                        .sorted(SchoolClass.COMPARATOR_BY_NAME)
                        .map(this::toSchoolClassJson)
                        .collect(StreamUtils.toJsonArray())
        );
        data.add(
                "lessons",
                shift.getAssociatedLessonsSet()
                        .stream()
                        .flatMap(
                                lesson -> Stream.concat(
                                        lesson.getLessonInstancesSet().stream().map(this::toLessonInstanceJson),
                                        lesson.getAllLessonIntervalsWithoutInstanceDates()
                                                .stream()
                                                .map(interval -> toLessonWithIntervalJson(lesson, interval))
                                )
                        )
                        .sorted(Comparator.comparing(lesson -> lesson.get("start").getAsString()))
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

    protected @NotNull JsonObject toSchoolClassJson(SchoolClass schoolClass) {
        return JsonUtils.toJson(data -> {
            data.addProperty("name", schoolClass.getNome());
            data.addProperty("curricularYear", schoolClass.getAnoCurricular());
            data.add("degree", toExtendedDegreeJson(schoolClass.getExecutionDegree().getDegree()));
        });
    }

    private enum OccupationType {
        GENERIC, LESSON, EVALUATION
    }

    protected @NotNull JsonObject toBasicSpaceJson(@NotNull Space space) {
        final Optional<SpaceType> type = SpaceType.getSpaceType(space);
        return JsonUtils.toJson(data -> {
            data.addProperty("id", space.getExternalId());
            data.addProperty("name", space.getName());
            data.addProperty("fullName", space.getFullName());
            data.addProperty("type", type.map(SpaceType::name).orElse("UNKNOWN"));
            data.add("classification", space.getClassification().getName().json());
        });
    }

    protected @NotNull JsonObject toSpaceJson(@NotNull Space space) {
        JsonObject data = toBasicSpaceJson(space);
        if (SpaceUtils.isRoom(space)) {
            data.add("capacity", JsonUtils.toJson(capacity -> {
                capacity.addProperty("regular", space.getAllocatableCapacity());
                capacity.addProperty("exam", SpaceUtils.countAvailableSeatsForExams(space));
            }));
        }
        data.addProperty("description", space.getPresentationName());
        // possibly only add the building field if a building contains the space - with campi and buildings probably not needed
        addIfAndFormatElement(data, "building", SpaceUtils.getSpaceBuilding(space), this::toBasicSpaceJson);
        addIfAndFormatElement(data, "campus", SpaceUtils.getSpaceCampus(space), this::toBasicSpaceJson);
        addIfAndFormatElement(data, "containedIn", space.getParent(), this::toBasicSpaceJson);
        data.add(
                "contains",
                space.getChildren()
                        .stream()
                        .map(this::toBasicSpaceJson)
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

    protected @NotNull JsonObject toExtendedSpaceJson(@NotNull Space space, @NotNull Interval interval) {
        JsonObject data = toSpaceJson(space);
        final Set<Occupation> occupations = space.getOccupationSet();
        data.add(
                "schedule",
                occupations.stream()
                        .flatMap(
                                occupation -> toOccupationJson(occupation, interval)
                        )
                        .sorted(Comparator.comparing(obj -> obj.get("start").getAsString()))
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

    /**
     * @param occupation the occupation to serialize
     * @param interval   the interval in which to get events;
     *                   events that don't overlap with this interval should be discarded
     * @return a stream of events, as {@link JsonObject JsonObjects}
     */
    private @NotNull Stream<@NotNull JsonObject> toOccupationJson(@NotNull Occupation occupation,
                                                                  @NotNull Interval interval) {
        if (occupation instanceof EventSpaceOccupation) {
            return getEventSpaceOccupationEvents((EventSpaceOccupation) occupation, interval);
        }
        return getSpaceGenericEvents(occupation, interval);
    }

    /**
     * @see BaseController#toOccupationJson(Occupation, Interval)
     */
    private @NotNull Stream<@NotNull JsonObject> getEventSpaceOccupationEvents(@NotNull EventSpaceOccupation occupation,
                                                                               @NotNull Interval interval) {
        return occupation.getEventSpaceOccupationIntervals(interval.getStart(), interval.getEnd())
                .stream()
                .map(eventInterval -> {
                    if (occupation instanceof LessonSpaceOccupation) {
                        return toLessonEventJson((LessonSpaceOccupation) occupation, interval);
                    } else if (occupation instanceof LessonInstanceSpaceOccupation) {
                        return toLessonInstanceEventJson((LessonInstanceSpaceOccupation) occupation, interval);
                    } else if (occupation instanceof WrittenEvaluationSpaceOccupation) {
                        return toWrittenEvaluationEventJson((WrittenEvaluationSpaceOccupation) occupation, interval);
                    }
                    return toGenericEventJson(occupation, eventInterval);
                });
    }

    /**
     * @see BaseController#toOccupationJson(Occupation, Interval)
     */
    private @NotNull Stream<@NotNull JsonObject> getSpaceGenericEvents(@NotNull Occupation occupation,
                                                                       @NotNull Interval interval) {
        return occupation.getIntervals()
                .stream()
                .filter(interval::overlaps)
                .map(eventInterval -> toGenericEventJson(occupation, eventInterval));
    }

    protected @NotNull JsonObject toGenericEventJson(@NotNull Occupation occupation,
                                                     @NotNull Interval occupationInterval) {
        return JsonUtils.toJson(event -> {
            event.addProperty("start", occupationInterval.getStart().toString());
            event.addProperty("end", occupationInterval.getEnd().toString());
            event.addProperty("name", occupation.getSubject());
            if (occupation.getConfig() != null) {
                event.addProperty("description", occupation.getSummary());
                event.addProperty("extendedDescription", occupation.getExtendedSummary());
            }
            addIfAndFormat(event, "url", occupation.getUrl(), url -> url.isEmpty() ? null : url);
            event.addProperty("type", OccupationType.GENERIC.name()); // this might be overwritten by other event types
        });
    }

    protected @NotNull JsonObject toLessonEventJson(@NotNull LessonSpaceOccupation occupation,
                                                    @NotNull Interval lessonInterval) {
        JsonObject event = toGenericEventJson(occupation, lessonInterval);
        final Lesson lesson = occupation.getLesson();
        event.addProperty("type", OccupationType.LESSON.name());
        event.add("shift", toShiftJson(lesson.getShift()));
        event.add("course", toExecutionCourseJson(lesson.getExecutionCourse()));
        return event;
    }

    protected @NotNull JsonObject toLessonInstanceEventJson(@NotNull LessonInstanceSpaceOccupation occupation,
                                                            @NotNull Interval lessonInterval) {
        JsonObject event = toGenericEventJson(occupation, lessonInterval);
        // It seems like all lesson instances are of the same lesson, they're just different days
        final Lesson lesson = occupation.getLessonInstancesSet().iterator().next().getLesson();
        event.addProperty("type", OccupationType.LESSON.name());
        event.add("shift", toShiftJson(lesson.getShift()));
        event.add("course", toExecutionCourseJson(lesson.getExecutionCourse()));
        return event;
    }

    protected @NotNull JsonObject toWrittenEvaluationEventJson(@NotNull WrittenEvaluationSpaceOccupation occupation,
                                                               @NotNull Interval evaluationInterval) {
        JsonObject event = toGenericEventJson(occupation, evaluationInterval);
        // There is always at least an evaluation because of interval checking
        // Apparently, there is almost always only one evaluation in the set, so we can ignore the other ones
        final WrittenEvaluation evaluation = occupation.getWrittenEvaluationsSet().iterator().next();
        event.addProperty("type", OccupationType.EVALUATION.name());
        event.add("evaluation", toExtendedEvaluationJson(evaluation));
        return event;
    }

    protected @NotNull JsonObject toStudentGroupJson(@NotNull StudentGroup studentGroup) {
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

    protected @NotNull JsonObject toTeacherJson(@NotNull Teacher teacher) {
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

    protected <T> void addIfAndFormat(@NotNull JsonObject object,
                                      @NotNull String key,
                                      @Nullable T value,
                                      @NotNull Function<@NotNull T, @Nullable String> format) {
        if (value != null) {
            String formattedValue = format.apply(value);
            if (formattedValue != null) {
                object.addProperty(key, formattedValue);
            }
        }
    }

    protected <T> void addIfAndFormatElement(@NotNull JsonObject object,
                                             @NotNull String key,
                                             @Nullable T value,
                                             Function<@NotNull T, @Nullable JsonElement> format) {
        if (value != null) {
            JsonElement formattedValue = format.apply(value);
            if (formattedValue != null) {
                object.add(key, formattedValue);
            }
        }
    }

}
