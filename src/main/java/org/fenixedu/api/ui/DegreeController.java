package org.fenixedu.api.ui;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicInterval;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/fenixedu-api/v2")
public class DegreeController extends BaseController {

    @RequestMapping(value = "/degrees", method = RequestMethod.GET)
    public ResponseEntity<?> degrees(@RequestParam(required = false) Optional<String> year) {
        final ExecutionYear executionYear = year.map(this::parseExecutionYearOrThrow)
                .orElseGet(ExecutionYear::readCurrentExecutionYear);
        final AcademicInterval academicInterval = executionYear.getAcademicInterval();
        return respond(
                ExecutionDegree.filterByAcademicInterval(academicInterval)
                        .stream()
                        .map(ExecutionDegree::getDegree)
                        .map(this::toDegreeJson)
        );
    }

    @RequestMapping(value = "/degrees/all", method = RequestMethod.GET)
    public ResponseEntity<?> getAllDegrees() {
        return respond(
                Degree.readNotEmptyDegrees()
                        .stream()
                        .map(this::toDegreeJson)
        );
    }

    @RequestMapping(value = "/degrees/{degree}", method = RequestMethod.GET)
    public ResponseEntity<?> getDegree(@PathVariable Degree degree) {
        return ok(toExtendedDegreeJson(degree));
    }

    @RequestMapping(value = "/degrees/{degree}/courses", method = RequestMethod.GET)
    public ResponseEntity<?> getDegreeCourses(@PathVariable Degree degree,
                                              @RequestParam(required = false) Optional<String> year) {
        // defaults to the current year if not specified
        final ExecutionYear executionYear = year.map(this::parseExecutionYearOrThrow)
                .orElseGet(ExecutionYear::readCurrentExecutionYear);
        return respond(
                degree.getAllCurricularCourses(executionYear)
                        .stream()
                        .map(course -> toCurricularCourseJson(course, executionYear))
        );
    }

}
