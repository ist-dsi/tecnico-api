package pt.ist.tecnicoapi.ui;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.Department;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.bennu.core.domain.Bennu;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/tecnico-api/v2")
public class DepartmentsController extends BaseController {

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping("/departments")
    protected ResponseEntity<?> getDepartments() {
        // default is to return currently active departments
        return respond(
                Department.readActiveDepartments()
                        .stream()
                        // these already come sorted by name
                        .map(this::toDepartmentJson)
        );
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping("/departments/all")
    protected ResponseEntity<?> getAllDepartments() {
        // return all departments (currently active or not)
        return respond(
                Bennu.getInstance()
                        .getDepartmentsSet()
                        .stream()
                        .sorted(Department.COMPARATOR_BY_NAME)
                        .map(this::toDepartmentJson)
        );
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping("/departments/{department}")
    protected ResponseEntity<?> getDepartment(@PathVariable Department department) {
        return ok(toExtendedDepartmentJson(department));
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping("/departments/{department}/competence-courses")
    protected ResponseEntity<?> getDepartmentCompetenceCourses(@PathVariable Department department) {
        return respond(
                department.getCompetenceCoursesSet()
                        .stream()
                        .sorted(CompetenceCourse.COMPETENCE_COURSE_COMPARATOR_BY_NAME)
                        .map(this::toCompetenceCourseJson)
        );
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping("/departments/{department}/execution-courses")
    protected ResponseEntity<?> getDepartmentExecutionCourses(@PathVariable Department department,
                                                              @RequestParam(required = false) Optional<String> year
    ) {
        final ExecutionYear executionYear = year.map(this::parseExecutionYearOrThrow)
                .orElseGet(ExecutionYear::readCurrentExecutionYear);
        return respond(
                executionYear.getExecutionPeriodsSet()
                        .stream()
                        .flatMap(
                                executionSemester -> department.getDepartmentUnit()
                                        .getAllExecutionCoursesByExecutionPeriod(executionSemester)
                                        .stream()
                        )
                        .sorted(ExecutionCourse.EXECUTION_COURSE_COMPARATOR_BY_EXECUTION_PERIOD_REVERSED_AND_NAME)
                        .map(this::toExecutionCourseJson)
        );
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping("/departments/{department}/scientific-areas")
    protected ResponseEntity<?> getDepartmentScientificAreas(@PathVariable Department department) {
        return respond(
                department.getDepartmentUnit()
                        .getScientificAreaUnits()
                        .stream()
                        // these already come sorted by name and ID
                        .map(this::toScientificAreaJson) // TODO: add teachers too
        );
    }
}
