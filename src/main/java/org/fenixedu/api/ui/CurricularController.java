package org.fenixedu.api.ui;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.*;
import org.fenixedu.academic.domain.degreeStructure.ProgramConclusion;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.academic.domain.student.curriculum.ICurriculum;
import org.fenixedu.academic.domain.student.curriculum.ICurriculumEntry;
import org.fenixedu.academic.domain.studentCurriculum.Dismissal;
import org.fenixedu.api.util.APIScope;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.commons.stream.StreamUtils;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/fenixedu-api/v2")
public class CurricularController extends BaseController {

    @RequestMapping(value = "/student/enrolments", method = RequestMethod.GET)
    public ResponseEntity<?> studentEnrolments(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
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
    public ResponseEntity<?> studentCurriculum(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken) {
        requireOAuthScope(accessToken, APIScope.STUDENT_READ);

        final Person person = Authenticate.getUser().getPerson();
        final Student student = person.getStudent();

        if (student == null) {
            return ok(new JsonArray());
        }

        return respond(
                student.getRegistrationsSet()
                        .stream()
                        .map(this::toRegistrationJsonWithCurriculumInformation)
        );
    }

    @RequestMapping(value = "/teacher/professorships", method = RequestMethod.GET)
    public ResponseEntity<?> teacherProfessorships(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
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

    protected JsonObject toRegistrationJsonWithCurriculumInformation(Registration registration) {
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

    private JsonObject toICurriculumEntryJson(ICurriculumEntry curriculumEntry) {
        return JsonUtils.toJson(data -> {
            data.add("name", curriculumEntry.getPresentationName().json());
            data.addProperty("grade", curriculumEntry.getGradeValue());
            data.addProperty("ects", curriculumEntry.getEctsCreditsForCurriculum());
            data.add("semester", toExecutionSemesterJson(curriculumEntry.getExecutionPeriod(), true));
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

    private JsonObject toEnrolmentJson(Enrolment enrolment, ExecutionSemester executionSemester) {
        return JsonUtils.toJson(data -> {
            data.addProperty("grade", enrolment.getGradeValue());
            data.addProperty("ects", enrolment.getEctsCredits());
            data.add("course", toExecutionCourseJson(enrolment.getExecutionCourseFor(executionSemester)));
        });
    }

    private JsonObject toProfessorshipJson(Professorship professorship) {
        return JsonUtils.toJson(data -> {
            data.addProperty("isResponsibleFor", professorship.isResponsibleFor());
            data.add("course", toExecutionCourseJson(professorship.getExecutionCourse()));
        });
    }

}
