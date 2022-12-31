package org.fenixedu.api.serializer;

import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.curricularPeriod.CurricularPeriod;
import org.fenixedu.academic.domain.degreeStructure.Context;
import org.fenixedu.academic.domain.degreeStructure.CourseGroup;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

import java.util.List;

public class CurricularCourseSerializer extends DomainObjectSerializer {

    protected CurricularCourseSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull CurricularCourse curricularCourse,
                                         @NotNull ExecutionYear executionYear) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", curricularCourse.getExternalId());
            data.add("name", curricularCourse.getNameI18N().json());
            data.addProperty("acronym", curricularCourse.getAcronym());
            data.addProperty("credits", curricularCourse.getEctsCredits(executionYear));
            data.add(
                    "executions",
                    curricularCourse.getExecutionCoursesByExecutionYear(executionYear)
                            .stream()
                            .map(this.getAPISerializer().getExecutionCourseSerializer()::serialize)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

    public @NotNull JsonObject serializeWithoutExecutions(@NotNull CurricularCourse curricularCourse,
                                                          @NotNull ExecutionSemester executionSemester) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", curricularCourse.getExternalId());
            data.add("name", curricularCourse.getNameI18N().json());
            data.addProperty("acronym", curricularCourse.getAcronym());
            data.addProperty("credits", curricularCourse.getEctsCredits(executionSemester));
        });
    }

    /**
     * Serialize a {@link Context} that wraps a {@link CurricularCourse}.
     *
     * @param context       A {@link Context} that wraps a {@link CurricularCourse}. If it does not wrap a course, it
     *                      will throw an error!
     * @param executionYear The year to fetch course data from.
     * @param pathTaken     The path that has been taken until the current course group (inclusive).
     * @return Serialized data
     */
    public @NotNull JsonObject serializeWithCurricularInformation(@NotNull Context context,
                                                                  @NotNull ExecutionYear executionYear,
                                                                  @NotNull List<CourseGroup> pathTaken) {
        final CurricularCourse course = (CurricularCourse) context.getChildDegreeModule();
        final CurricularPeriod period = context.getCurricularPeriod();
        // "term" is the half of the semester when the course occurs, or null if it's the whole semester
        final Integer term = context.getTerm();

        final JsonObject data = serialize(course, executionYear);
        data.add("workload", JsonUtils.toJson(workload -> {
            workload.addProperty("autonomous", course.getAutonomousWorkHours());
            workload.addProperty("contact", course.getContactLoad());
            workload.addProperty("total", course.getTotalLoad());
        }));
        addIfAndFormatElement(
                data,
                "curricularPeriod",
                period,
                p -> getAPISerializer().getCurricularPeriodSerializer().serialize(p, term)
        );
        data.addProperty("executionInterval", course.getRegime().getLocalizedName());
        data.addProperty("optional", course.isOptional());
        data.add(
                "courseGroups",
                pathTaken
                        .stream()
                        .map(getAPISerializer().getCourseGroupSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

}
