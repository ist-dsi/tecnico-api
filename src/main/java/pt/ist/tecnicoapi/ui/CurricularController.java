package pt.ist.tecnicoapi.ui;


import com.google.gson.JsonArray;
import net.fortuna.ical4j.model.Calendar;
import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.WrittenEvaluation;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.academic.domain.util.icalendar.CalendarFactory;
import org.fenixedu.academic.domain.util.icalendar.EventBean;
import org.fenixedu.academic.service.services.exceptions.FenixServiceException;
import org.fenixedu.academic.service.services.exceptions.InvalidArgumentsServiceException;
import org.fenixedu.academic.service.services.student.EnrolStudentInWrittenEvaluation;
import org.fenixedu.academic.service.services.student.UnEnrollStudentInWrittenEvaluation;
import org.fenixedu.academic.ui.struts.action.ICalendarSyncPoint;
import pt.ist.tecnicoapi.util.APIError;
import pt.ist.tecnicoapi.util.APIScope;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.security.SkipCSRF;
import org.jetbrains.annotations.NotNull;
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
@RequestMapping("/tecnico-api/v2")
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

    // Just like stated in the OpenAPI documentation, "attending"-related fields are related to execution
    // courses where the student is able to attend shifts, while "enrolled"-related fields are related to
    // courses where the student is able to be graded
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

        return respond(
                semesters.stream()
                        .flatMap(
                                executionSemester -> student.getRegistrationStream()
                                        .flatMap(registration -> registration.getEnrolments(executionSemester).stream())
                                        .map(enrolment -> toEnrolmentJson(enrolment, executionSemester))
                        )
        );
    }

    // Just like stated in the OpenAPI documentation, "attending"-related fields are related to execution
    // courses where the student is able to attend shifts, while "enrolled"-related fields are related to
    // courses where the student is able to be graded
    @RequestMapping(value = "/student/attending", method = RequestMethod.GET)
    public ResponseEntity<?> getStudentCoursesAttending(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
                                                        @RequestParam(required = false) Optional<String> year,
                                                        @RequestParam(required = false) Optional<Integer> semester) {
        requireOAuthScope(accessToken, APIScope.STUDENT_READ);

        final Collection<ExecutionSemester> semesters = parseExecutionSemestersOrThrow(year, semester);

        final Person person = Authenticate.getUser().getPerson();
        final Student student = person.getStudent();

        if (student == null) {
            return ok(new JsonArray());
        }

        return respond(
                semesters.stream()
                        .flatMap(
                                executionSemester -> student.getRegistrationStream()
                                        .flatMap(
                                                registration -> registration.getAttendsForExecutionPeriod(
                                                        executionSemester
                                                ).stream()
                                        )
                                        .map(this::toAttendsJson)
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
                throw new APIError(HttpStatus.CONFLICT, "error.evaluation.enrolment.already.enrolled");
            } else if (Objects.equals(key, "error.notAuthorizedUnEnrollment") || Objects.equals(
                    key,
                    "error.enrolmentPeriodNotDefined"
            )) {
                throw new APIError(HttpStatus.FORBIDDEN, "error.evaluation.enrolment.period.closed");
            } else if (Objects.equals(key, "error.studentNotEnrolled")) {
                throw new APIError(HttpStatus.CONFLICT, "error.evaluation.enrolment.not.enrolled");
            }
            throw new APIError(HttpStatus.INTERNAL_SERVER_ERROR, "error.evaluation.enrolment.unknown.error");
        }
    }

}
