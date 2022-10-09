package org.fenixedu.api.ui;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fortuna.ical4j.model.Calendar;
import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.Evaluation;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.WrittenEvaluation;
import org.fenixedu.academic.domain.degreeStructure.ProgramConclusion;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.academic.domain.student.curriculum.ICurriculum;
import org.fenixedu.academic.domain.student.curriculum.ICurriculumEntry;
import org.fenixedu.academic.domain.studentCurriculum.Dismissal;
import org.fenixedu.academic.domain.util.icalendar.CalendarFactory;
import org.fenixedu.academic.domain.util.icalendar.ClassEventBean;
import org.fenixedu.academic.domain.util.icalendar.EvaluationEventBean;
import org.fenixedu.academic.domain.util.icalendar.EventBean;
import org.fenixedu.academic.service.services.exceptions.FenixServiceException;
import org.fenixedu.academic.service.services.exceptions.InvalidArgumentsServiceException;
import org.fenixedu.academic.service.services.student.EnrolStudentInWrittenEvaluation;
import org.fenixedu.academic.service.services.student.UnEnrollStudentInWrittenEvaluation;
import org.fenixedu.academic.ui.struts.action.ICalendarSyncPoint;
import org.fenixedu.api.util.APIError;
import org.fenixedu.api.util.APIScope;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.security.SkipCSRF;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/fenixedu-api/v2")
public class CurricularController extends BaseController {

    @RequestMapping(value = "/student/calendar/{type:classes|evaluations}", method = RequestMethod.GET)
    public ResponseEntity<?> getStudentCalendar(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
                                                @PathVariable String type,
                                                @RequestParam(defaultValue = "json") String format) {
        requireOAuthScope(accessToken, APIScope.STUDENT_READ);

        final User user = Authenticate.getUser();
        ICalendarSyncPoint calendarSyncPoint = new ICalendarSyncPoint();

        List<EventBean> events;
        if ("classes".equalsIgnoreCase(type)) {
            events = calendarSyncPoint.getClasses(user);
        } else {
            events = calendarSyncPoint.getExams(user);
        }

        return respondCalendar(events, format);
    }

    @RequestMapping(value = "/teacher/calendar/{type:classes|evaluations}", method = RequestMethod.GET)
    public ResponseEntity<?> getTeacherCalendar(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
                                                @PathVariable String type,
                                                @RequestParam(defaultValue = "json") String format) {
        requireOAuthScope(accessToken, APIScope.TEACHER_READ);

        final User user = Authenticate.getUser();
        ICalendarSyncPoint calendarSyncPoint = new ICalendarSyncPoint();

        List<EventBean> events;
        if ("classes".equalsIgnoreCase(type)) {
            events = calendarSyncPoint.getTeachingClasses(user);
        } else {
            events = calendarSyncPoint.getTeachingExams(user);
        }

        return respondCalendar(events, format);
    }

    private ResponseEntity<?> respondCalendar(List<EventBean> events, String format) {
        if ("json".equalsIgnoreCase(format)) {
            return respond(events.stream().map(this::toEventBeanJson));
        } else if ("ical".equalsIgnoreCase(format)) {
            Calendar calendar = CalendarFactory.createCalendar(events);

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(new MediaType("text", "calendar", StandardCharsets.UTF_8))
                    .body(calendar.toString());
        }
        throw new APIError(HttpStatus.BAD_REQUEST, "error.calendar.format.incorrect", format);
    }

    @RequestMapping(value = "/student/enrolments", method = RequestMethod.GET)
    public ResponseEntity<?> getStudentEnrolments(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
                                                  @RequestParam(required = false) Optional<String> year,
                                                  @RequestParam(required = false) Optional<Integer> semester) {
        requireOAuthScope(accessToken, APIScope.STUDENT_READ);

        final Collection<ExecutionSemester> semesters = parseExecutionSemestersOrThrow(year, semester);

        final Person person = Authenticate.getUser().getPerson();
        final Student student = person.getStudent();

        if (student == null) {
            return ok(new JsonArray());
        }

        // TODO The APIv1 has an "attending" field that we don't know what's for
        return respond(
                semesters.stream()
                        .flatMap(
                                executionSemester -> student.getRegistrationStream()
                                        .flatMap(registration -> registration.getEnrolments(executionSemester).stream())
                                        .map(enrolment -> toEnrolmentJson(enrolment, executionSemester))
                        )
        );
    }

    @RequestMapping(value = "/student/curriculum", method = RequestMethod.GET)
    public ResponseEntity<?> getStudentCurriculum(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken) {
        requireOAuthScope(accessToken, APIScope.STUDENT_READ);

        final Person person = Authenticate.getUser().getPerson();
        final Student student = person.getStudent();

        if (student == null) {
            return ok(new JsonArray());
        }

        return respond(
                student.getRegistrationsSet()
                        .stream()
                        .map(this::toRegistrationWithCurriculumInformationJson)
        );
    }

    @RequestMapping(value = "/teacher/professorships", method = RequestMethod.GET)
    public ResponseEntity<?> getTeacherProfessorships(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
                                                      @RequestParam(required = false) Optional<String> year,
                                                      @RequestParam(required = false) Optional<Integer> semester) {
        requireOAuthScope(accessToken, APIScope.TEACHER_READ);

        final Collection<ExecutionSemester> semesters = parseExecutionSemestersOrThrow(year, semester);

        final Person person = Authenticate.getUser().getPerson();

        return respond(
                person.getProfessorshipsSet()
                        .stream()
                        .filter(
                                professorship -> semesters.contains(
                                        professorship.getExecutionCourse().getExecutionPeriod()
                                )
                        )
                        .map(this::toProfessorshipJson)
        );
    }

    @RequestMapping(value = "/student/evaluations", method = RequestMethod.GET)
    public ResponseEntity<?> getStudentEvaluations(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
                                                   @RequestParam(required = false) Optional<String> year,
                                                   @RequestParam(required = false) Optional<Integer> semester) {
        requireOAuthScope(accessToken, APIScope.EVALUATIONS_READ);

        final Person person = Authenticate.getUser().getPerson();
        final Student student = person.getStudent();

        if (student == null) {
            return ok(new JsonArray());
        }

        final Collection<ExecutionSemester> semesters = parseExecutionSemestersOrThrow(year, semester);

        return respond(
                student.getRegistrationsSet()
                        .stream()
                        .flatMap(
                                registration -> registration.getAssociatedAttendsSet()
                                        .stream()
                                        .filter(attends -> semesters.contains(attends.getExecutionPeriod()))
                                        .map(Attends::getExecutionCourse)
                                        .flatMap(
                                                course -> course.getAssociatedEvaluationsSet()
                                                        .stream()
                                                        .map(
                                                                evaluation -> toEvaluationWithEnrolmentStateJson(
                                                                        evaluation,
                                                                        course,
                                                                        registration
                                                                )
                                                        )
                                        )
                        )
        );
    }

    @SkipCSRF
    @RequestMapping(value = "/student/evaluations/{evaluation}", method = RequestMethod.PUT)
    public ResponseEntity<?> enrolStudentInEvaluation(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
                                                      @PathVariable WrittenEvaluation evaluation) {
        requireOAuthScope(accessToken, APIScope.EVALUATIONS_WRITE);

        final Person person = Authenticate.getUser().getPerson();

        enrolOrUnenrolInWrittenEvaluation(person, evaluation, true);

        return ResponseEntity.noContent().build();
    }

    @SkipCSRF
    @RequestMapping(value = "/student/evaluations/{evaluation}", method = RequestMethod.DELETE)
    public ResponseEntity<?> unEnrolStudentFromEvaluation(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
                                                          @PathVariable WrittenEvaluation evaluation) {
        requireOAuthScope(accessToken, APIScope.EVALUATIONS_WRITE);

        final Person person = Authenticate.getUser().getPerson();

        enrolOrUnenrolInWrittenEvaluation(person, evaluation, false);

        return ResponseEntity.noContent().build();
    }

    private void enrolOrUnenrolInWrittenEvaluation(@NotNull Person person,
                                                   @NotNull WrittenEvaluation evaluation,
                                                   boolean enrol) {
        try {
            if (enrol) {
                // FIXME fenixedu-academic should be updated to receive proper parameters here
                EnrolStudentInWrittenEvaluation.runEnrolStudentInWrittenEvaluation(
                        person.getUsername(),
                        evaluation.getExternalId()
                );
            } else {
                // FIXME fenixedu-academic should be updated to receive proper parameters here
                UnEnrollStudentInWrittenEvaluation.runUnEnrollStudentInWrittenEvaluation(
                        person.getUsername(),
                        evaluation.getExternalId()
                );
            }
        } catch (InvalidArgumentsServiceException e) {
            // student does not have an active registration in a course having this evaluation
            throw new APIError(HttpStatus.FORBIDDEN, "error.evaluation.enrolment.no.registration");
        } catch (FenixServiceException e) {
            final String message = e.getMessage();
            if (Objects.equals(message, "Enrolment period is not opened")) {
                throw new APIError(HttpStatus.FORBIDDEN, "error.evaluation.enrolment.period.closed");
            }
            throw new APIError(HttpStatus.INTERNAL_SERVER_ERROR, "error.evaluation.enrolment.unknown.error");
        } catch (DomainException e) {
            final String key = e.getKey();
            if (Objects.equals(key, "error.alreadyEnrolledError")) {
                throw new APIError(HttpStatus.CONFLICT, "error.evaluation.enrolment.already.enroled");
            } else if (Objects.equals(key, "error.notAuthorizedUnEnrollment") || Objects.equals(
                    key,
                    "error.enrolmentPeriodNotDefined"
            )) {
                throw new APIError(HttpStatus.FORBIDDEN, "error.evaluation.enrolment.period.closed");
            } else if (Objects.equals(key, "error.studentNotEnroled")) {
                throw new APIError(HttpStatus.CONFLICT, "error.evaluation.enrolment.not.enroled");
            }
            throw new APIError(HttpStatus.INTERNAL_SERVER_ERROR, "error.evaluation.enrolment.unknown.error");
        }
    }

    protected @NotNull JsonObject toRegistrationWithCurriculumInformationJson(@NotNull Registration registration) {
        JsonObject data = toRegistrationJson(registration);

        final StudentCurricularPlan lastCurricularPlan = registration.getLastStudentCurricularPlan();

        data.addProperty(
                "startDate",
                registration.getFirstStudentCurricularPlan().getStartDateYearMonthDay().toString()
        );
        addIfAndFormat(data, "endDate", lastCurricularPlan.getEndDate(), YearMonthDay::toString);

        final ICurriculum curriculum = lastCurricularPlan.getCurriculum(new DateTime(), null);

        data.addProperty("curricularYear", curriculum.getCurricularYear());
        data.addProperty("credits", curriculum.getSumEctsCredits());
        data.addProperty("average", curriculum.getRawGrade().getNumericValue());
        data.addProperty("roundedAverage", curriculum.getFinalGrade().getIntegerValue());
        data.addProperty("isConcluded", registration.isConcluded());

        data.add(
                "approvedCourses",
                registration.getStudentCurricularPlanStream()
                        // Get all groups for conclusion
                        .flatMap(
                                curricularPlan -> ProgramConclusion.conclusionsFor(curricularPlan)
                                        .map(programConclusion -> programConclusion.groupFor(curricularPlan))
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                        )
                        .flatMap(curriculumGroup -> curriculumGroup.getCurriculum().getCurriculumEntries().stream())
                        .map(this::toICurriculumEntryJson)
                        .collect(StreamUtils.toJsonArray())
        );

        return data;
    }

    private @NotNull JsonObject toICurriculumEntryJson(@NotNull ICurriculumEntry curriculumEntry) {
        return JsonUtils.toJson(data -> {
            data.add("name", curriculumEntry.getPresentationName().json());
            data.addProperty("grade", curriculumEntry.getGradeValue());
            data.addProperty("ects", curriculumEntry.getEctsCreditsForCurriculum());
            data.add("semester", toExtendedExecutionSemesterJson(curriculumEntry.getExecutionPeriod()));
            if (curriculumEntry instanceof Enrolment) {
                Enrolment enrolment = (Enrolment) curriculumEntry;
                CurricularCourse course = enrolment.getCurricularCourse();
                data.addProperty("type", "ENROLMENT");
                addIfAndFormatElement(
                        data,
                        "course",
                        course,
                        c -> toCurricularCourseJson(c, curriculumEntry.getExecutionYear())
                );
                addIfAndFormat(
                        data,
                        "url",
                        enrolment.getExecutionCourseFor(curriculumEntry.getExecutionPeriod()),
                        ExecutionCourse::getSiteUrl
                );
            } else if (curriculumEntry instanceof Dismissal) {
                CurricularCourse course = ((Dismissal) curriculumEntry).getCurricularCourse();
                data.addProperty("type", "DISMISSAL");
                addIfAndFormatElement(
                        data,
                        "course",
                        course,
                        c -> toCurricularCourseJson(c, curriculumEntry.getExecutionYear())
                );
            } else {
                data.addProperty("type", "OTHER");
            }
        });
    }

    private @NotNull JsonObject toEnrolmentJson(@NotNull Enrolment enrolment,
                                                @NotNull ExecutionSemester executionSemester) {
        return JsonUtils.toJson(data -> {
            data.addProperty("grade", enrolment.getGradeValue());
            data.addProperty("ects", enrolment.getEctsCredits());
            data.add("course", toExecutionCourseJson(enrolment.getExecutionCourseFor(executionSemester)));
        });
    }

    private @NotNull JsonObject toProfessorshipJson(@NotNull Professorship professorship) {
        return JsonUtils.toJson(data -> {
            data.addProperty("isResponsibleFor", professorship.isResponsibleFor());
            data.add("course", toExecutionCourseJson(professorship.getExecutionCourse()));
        });
    }

    private @NotNull JsonObject toEvaluationWithEnrolmentStateJson(@NotNull Evaluation evaluation,
                                                                   @NotNull ExecutionCourse course,
                                                                   @NotNull Registration registration) {
        final JsonObject data = toExtendedEvaluationJson(evaluation);
        data.add("course", toExecutionCourseJson(course));

        if (evaluation instanceof WrittenEvaluation) {
            boolean isEnroled = ((WrittenEvaluation) evaluation).getWrittenEvaluationEnrolmentFor(registration) != null;
            data.addProperty("isEnroled", isEnroled);
        }

        return data;
    }

    private @NotNull JsonObject toEventBeanJson(@NotNull EventBean event) {
        if (event instanceof ClassEventBean) {
            return toClassEventBeanJson((ClassEventBean) event);
        } else if (event instanceof EvaluationEventBean) {
            return toEvaluationEventBeanJson((EvaluationEventBean) event);
        }
        return toGenericEventBeanJson(event);
    }

    private @NotNull JsonObject toGenericEventBeanJson(@NotNull EventBean event) {
        return JsonUtils.toJson(data -> {
            data.addProperty("start", event.getBegin().toString());
            data.addProperty("end", event.getBegin().toString());

            data.add("title", fakeLocalizedString(event.getOriginalTitle()).json());

            addIfAndFormatElement(
                    data,
                    "locations",
                    event.getRooms(),
                    rooms -> rooms.stream().map(this::toBasicSpaceJson).collect(StreamUtils.toJsonArray())
            );
        });
    }

    private @NotNull JsonObject toClassEventBeanJson(@NotNull ClassEventBean event) {
        JsonObject data = toGenericEventBeanJson(event);
        data.add("shift", toShiftJson(event.getClassShift()));
        data.add("course", toExecutionCourseJson(event.getClassShift().getExecutionCourse()));

        return data;
    }

    private @NotNull JsonObject toEvaluationEventBeanJson(@NotNull EvaluationEventBean event) {
        JsonObject data = toGenericEventBeanJson(event);
        addIfAndFormatElement(data, "assignedRoom", event.getAssignedRoom(), this::toBasicSpaceJson);
        data.add(
                "courses",
                event.getCourses().stream().map(this::toExecutionCourseJson).collect(StreamUtils.toJsonArray())
        );

        return data;
    }

    /**
     * EventBean title's is not a {@link LocalizedString}.
     * While that is not fixed, this will create a localized string with the same text across all languages.
     * This method SHOULD and WILL be removed in the future.
     *
     * @param string The string to replicate on all languages.
     * @return A LocalizedString with the same text for all supported languages.
     */
    private @NotNull LocalizedString fakeLocalizedString(@NotNull String string) {
        return CoreConfiguration.supportedLocales()
                .stream()
                .reduce(new LocalizedString.Builder(), (ls, locale) -> ls.with(locale, string), (ls1, ls2) -> ls1)
                .build();
    }

}
