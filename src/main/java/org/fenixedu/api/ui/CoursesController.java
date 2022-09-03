package org.fenixedu.api.ui;

import org.fenixedu.academic.domain.AdHocEvaluation;
import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.Project;
import org.fenixedu.academic.domain.WrittenEvaluation;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.gson.JsonObject;

@RestController
@RequestMapping("/fenixedu-api/v2")
public class CoursesController extends BaseController {

    @RequestMapping(value = "/courses/{executionCourse}", method = RequestMethod.GET)
    protected ResponseEntity<?> getCourse(@PathVariable final ExecutionCourse executionCourse) {
        return ok(toExtendedExecutionCourseJson(executionCourse));
    }

    @RequestMapping(value = "/courses/{executionCourse}/evaluations", method = RequestMethod.GET)
    protected ResponseEntity<?> getCourseEvaluations(@PathVariable final ExecutionCourse executionCourse) {
        return respond(
                executionCourse.getOrderedAssociatedEvaluations()
                        .stream()
                        .map(evaluation -> {
                            if (evaluation instanceof AdHocEvaluation) {
                                return toExtendedEvaluationJson((AdHocEvaluation) evaluation);
                            } else if (evaluation instanceof Project) {
                                return toExtendedEvaluationJson((Project) evaluation);
                            } else if (evaluation instanceof WrittenEvaluation) {
                                return toExtendedEvaluationJson((WrittenEvaluation) evaluation);
                            }
                            return toEvaluationJson(evaluation);
                        })
        );
    }

    @RequestMapping(value = "/courses/{executionCourse}/groups", method = RequestMethod.GET)
    protected ResponseEntity<?> getCourseGroups(@PathVariable final ExecutionCourse executionCourse) {
        return respond(executionCourse.getGroupings().stream().map(this::toGroupingJson));
    }

    @RequestMapping(value = "/courses/{executionCourse}/schedule", method = RequestMethod.GET)
    protected ResponseEntity<?> getCourseSchedule(@PathVariable final ExecutionCourse executionCourse) {
        // TODO: this should probably only be done when the spaces endpoint is completed
        return ok(new JsonObject());
    }

    @RequestMapping(value = "/courses/{executionCourse}/students", method = RequestMethod.GET)
    protected ResponseEntity<?> getCourseStudents(@PathVariable final ExecutionCourse executionCourse) {
        return ok(JsonUtils.toJson(data -> {
            data.addProperty("attendingCount", executionCourse.getAttendsSet().size());
            data.addProperty("enrolledCount", executionCourse.getEnrolmentCount());
            data.add(
                    "attendingStudents",
                    executionCourse.getAttendsSet()
                            .stream()
                            .sorted(Attends.COMPARATOR_BY_STUDENT_NUMBER)
                            .map(this::toAttendsJson)
                            .collect(StreamUtils.toJsonArray())
            );
        }));
    }

}
