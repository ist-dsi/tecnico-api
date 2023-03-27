package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.degreeStructure.CompetenceCourseInformation;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CompetenceCourseSerializer extends DomainObjectSerializer {

    protected CompetenceCourseSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull CompetenceCourse competenceCourse) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", competenceCourse.getExternalId());
            data.addProperty("name", competenceCourse.getName());
            data.addProperty("acronym", competenceCourse.getAcronym());
        });
    }

    public @NotNull JsonObject serializeExtended(@NotNull CompetenceCourse competenceCourse,
                                                 @NotNull Set<ExecutionSemester> executionSemesters) {
        JsonObject data = serialize(competenceCourse);

        // We'll get the competence course information from the most recent semester.
        // This is not ideal, but it's the only way to support getting the courses from the whole year.
        final ExecutionSemester mostRecentExecutionSemester = executionSemesters.stream()
                .min(ExecutionSemester.COMPARATOR_BY_SEMESTER_AND_YEAR)
                .orElseThrow(
                        () -> new IllegalArgumentException("Expected given execution semester set to not be empty")
                );
        final CompetenceCourseInformation competenceCourseInformation = competenceCourse
                .findCompetenceCourseInformationForExecutionPeriod(mostRecentExecutionSemester);

        data.add(
                "executionCourses",
                executionSemesters.stream()
                        .sorted(ExecutionSemester.COMPARATOR_BY_SEMESTER_AND_YEAR)
                        .flatMap(
                                executionSemester -> competenceCourse.getExecutionCoursesByExecutionPeriod(
                                        executionSemester
                                )
                                        .stream()
                                        .map(
                                                executionCourse -> getAPISerializer().getExecutionCourseSerializer()
                                                        .serialize(executionCourse)
                                        )
                        )
                        .collect(StreamUtils.toJsonArray())
        );
        data.add(
                "bibliographicReferences",
                competenceCourseInformation
                        .getBibliographicReferences()
                        .getBibliographicReferencesSortedByOrder()
                        .stream()
                        .map(getAPISerializer().getBibliographicReferenceSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        data.addProperty("regime", competenceCourseInformation.getRegime().getLocalizedName());
        data.add(
                "objectives",
                JsonUtils.toJson(objective -> {
                    objective.addProperty("pt-PT", competenceCourseInformation.getObjectives());
                    objective.addProperty("en-GB", competenceCourseInformation.getObjectivesEn());
                }
                )
        );
        data.add(
                "program",
                JsonUtils.toJson(program -> {
                    program.addProperty("pt-PT", competenceCourseInformation.getProgram());
                    program.addProperty("en-GB", competenceCourseInformation.getProgramEn());
                }
                )
        );
        return data;
    }

}
