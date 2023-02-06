package org.fenixedu.api.ui;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.degreeStructure.Context;
import org.fenixedu.academic.domain.degreeStructure.CourseGroup;
import org.fenixedu.academic.domain.time.calendarStructure.AcademicInterval;
import org.fenixedu.api.util.APIError;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/fenixedu-api/v2")
public class DegreeController extends BaseController {

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/degrees", method = RequestMethod.GET)
    public ResponseEntity<?> getDegreesOfYear(@RequestParam(required = false) final Optional<String> year) {
        final ExecutionYear executionYear = year.map(this::parseExecutionYearOrThrow)
                .orElseGet(ExecutionYear::readCurrentExecutionYear);
        final AcademicInterval academicInterval = executionYear.getAcademicInterval();
        return respond(
                ExecutionDegree.filterByAcademicInterval(academicInterval)
                        .stream()
                        .map(this::toExecutionDegreeJson)
        );
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/degrees/all", method = RequestMethod.GET)
    public ResponseEntity<?> getAllDegrees() {
        return respond(
                Degree.readNotEmptyDegrees()
                        .stream()
                        .map(this::toExtendedDegreeJson)
        );
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/degrees/{degree}", method = RequestMethod.GET)
    public ResponseEntity<?> getDegree(@PathVariable final Degree degree,
                                       @RequestParam(required = false) final Optional<String> year) {
        final ExecutionYear executionYear = year.map(this::parseExecutionYearOrThrow)
                .orElseGet(ExecutionYear::readCurrentExecutionYear);
        final ExecutionDegree executionDegree = degree.getExecutionDegreesForExecutionYear(executionYear)
                .stream()
                .findFirst()
                .orElseThrow(
                        () -> new APIError(
                                HttpStatus.NOT_FOUND,
                                "error.degree.does.not.exist.in.year",
                                degree.getExternalId(),
                                executionYear.getName()
                        )
                );
        return ok(toExtendedExecutionDegreeJson(executionDegree));
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/degrees/{degree}/courses", method = RequestMethod.GET)
    public ResponseEntity<?> getDegreeCourses(@PathVariable final Degree degree,
                                              @RequestParam(required = false) final Optional<String> year) {
        final ExecutionYear executionYear = year.map(this::parseExecutionYearOrThrow)
                .orElseGet(ExecutionYear::readCurrentExecutionYear);
        return respond(
                degree.getDegreeCurricularPlansForYear(executionYear)
                        .stream()
                        // Merge courses from all curricular plans (normally just one)
                        .flatMap(curricularPlan -> curricularPlan.getCurricularCoursesSet().stream())
                        .distinct()
                        .map(course -> toCurricularCourseJson(course, executionYear))
        );
    }

    // similar to the above, returning more information per course
    @RequestMapping(value = "/degrees/{degree}/curriculum", method = RequestMethod.GET)
    public ResponseEntity<?> getDegreeCurriculum(@PathVariable final Degree degree,
                                                 @RequestParam(required = false) final Optional<String> year) {
        final ExecutionYear executionYear = year.map(this::parseExecutionYearOrThrow)
                .orElseGet(ExecutionYear::readCurrentExecutionYear);
        final Collection<DegreeCurricularPlan> curricularPlans = degree.getDegreeCurricularPlansForYear(executionYear);
        if (curricularPlans.isEmpty()) {
            throw new APIError(
                    HttpStatus.NOT_FOUND,
                    "error.degree.does.not.exist.in.year",
                    degree.getExternalId(),
                    executionYear.getName()
            );
        }
        return respond(
                curricularPlans.stream()
                        .flatMap(
                                curricularPlan -> traverseCourseGroupTree(
                                        curricularPlan.getRoot(),
                                        Collections.emptyList(),
                                        executionYear
                                )
                        )
        );
    }

    /**
     * Recursively traverse {@link CourseGroup course groups} in the
     * {@link DegreeCurricularPlan curricular plan of the degree} as a tree.
     *
     * @param node          The current node {@link CourseGroup}.
     * @param pathTaken     The path that has been taken until the current node, or an empty list if root.
     * @param executionYear The {@link ExecutionYear year} we want to traverse in.
     * @return A stream of serialized {@link CurricularCourse courses} with extra information from {@link Context}.
     */
    private @NotNull Stream<JsonObject> traverseCourseGroupTree(@NotNull CourseGroup node,
                                                                @NotNull List<@NotNull CourseGroup> pathTaken,
                                                                @NotNull ExecutionYear executionYear) {
        List<CourseGroup> newPathTaken = new ArrayList<>(pathTaken.size() + 1);
        newPathTaken.addAll(pathTaken);
        newPathTaken.add(node);

        Stream<JsonObject> courses = node.getSortedOpenChildContextsWithCurricularCourses(executionYear)
                .stream()
                .map(
                        context -> toCurricularCourseWithCurricularInformationJson(
                                context,
                                executionYear,
                                newPathTaken
                        )
                );

        return Stream.concat(
                courses,
                node.getSortedOpenChildContextsWithCourseGroups(executionYear)
                        .stream()
                        .flatMap(
                                context -> traverseCourseGroupTree(
                                        (CourseGroup) context.getChildDegreeModule(),
                                        newPathTaken,
                                        executionYear
                                )
                        )
        );
    }

}
