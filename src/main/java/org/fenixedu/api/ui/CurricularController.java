package org.fenixedu.api.ui;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.api.util.APIScope;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.bennu.core.security.Authenticate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/fenixedu-api/v2")
public class CurricularController extends BaseController {

    @RequestMapping(value = "/student/enrolments", method = RequestMethod.GET)
    public ResponseEntity<?> person(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken,
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
                semesters.stream().flatMap(executionSemester ->
                        student.getRegistrationStream()
                                .flatMap(registration -> registration.getEnrolments(executionSemester).stream())
                                .map(enrolment -> toEnrolmentJson(enrolment, executionSemester))
                )
        );
    }

    private JsonObject toEnrolmentJson(Enrolment enrolment, ExecutionSemester executionSemester) {
        return JsonUtils.toJson(data -> {
            data.addProperty("grade", enrolment.getGradeValue());
            data.addProperty("ects", enrolment.getEctsCredits());
            data.add("course", toExecutionCourseJson(enrolment.getExecutionCourseFor(executionSemester)));
        });
    }

}
