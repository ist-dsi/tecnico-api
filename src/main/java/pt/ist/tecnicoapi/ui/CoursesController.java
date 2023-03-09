package pt.ist.tecnicoapi.ui;

import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.cms.domain.Post;
import org.fenixedu.commons.stream.StreamUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tecnico-api/v2")
public class CoursesController extends BaseController {

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/courses/{executionCourse}", method = RequestMethod.GET)
    protected ResponseEntity<?> getCourse(@PathVariable final ExecutionCourse executionCourse) {
        return ok(toExtendedExecutionCourseJson(executionCourse));
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/courses/{executionCourse}/announcements", method = RequestMethod.GET)
    protected ResponseEntity<?> getCourseAnnouncements(@PathVariable final ExecutionCourse executionCourse) {
        return respond(
                executionCourse.getSite()
                        .getCategoriesSet()
                        .stream()
                        .filter(category -> category.getSlug().equals("announcement"))
                        .flatMap(category -> category.getPostsSet().stream())
                        .filter(Post::isVisible)
                        .filter(post -> post.getCanViewGroup().isMember(null))
                        .sorted(Post.CREATION_DATE_COMPARATOR)
                        .map(this::toPostJson)
        );
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/courses/{executionCourse}/evaluations", method = RequestMethod.GET)
    protected ResponseEntity<?> getCourseEvaluations(@PathVariable final ExecutionCourse executionCourse) {
        return respond(
                executionCourse.getOrderedAssociatedEvaluations()
                        .stream()
                        .map(this::toExtendedEvaluationJson)
        );
    }

    @RequestMapping(value = "/courses/{executionCourse}/groups", method = RequestMethod.GET)
    protected ResponseEntity<?> getCourseGroups(@PathVariable final ExecutionCourse executionCourse) {
        return respond(executionCourse.getGroupings().stream().map(this::toGroupingJson));
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/courses/{executionCourse}/schedule", method = RequestMethod.GET)
    protected ResponseEntity<?> getCourseSchedule(@PathVariable final ExecutionCourse executionCourse) {
        return ok(toScheduleJson(executionCourse));
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
                            .map(Attends::getRegistration)
                            .map(this::toRegistrationForOthersJson)
                            .collect(StreamUtils.toJsonArray())
            );
        }));
    }

}
