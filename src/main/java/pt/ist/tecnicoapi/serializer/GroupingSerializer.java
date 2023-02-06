package pt.ist.tecnicoapi.serializer;

import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.Grouping;
import org.fenixedu.academic.domain.StudentGroup;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class GroupingSerializer extends DomainObjectSerializer {

    protected GroupingSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Grouping grouping) {
        return JsonUtils.toJson(data -> {
            data.addProperty("name", grouping.getName());
            data.addProperty("description", grouping.getProjectDescription());
            data.add("enrolmentPeriod", JsonUtils.toJson(period -> {
                period.addProperty("start", grouping.getEnrolmentBeginDayDateDateTime().toString());
                period.addProperty("end", grouping.getEnrolmentEndDayDateDateTime().toString());
                addIfAndFormatElement(
                        period,
                        "policy",
                        grouping.getEnrolmentPolicy(),
                        getAPISerializer().getEnrolmentPolicySerializer()::serialize
                );
            }));
            data.add("capacity", JsonUtils.toJson(capacity -> {
                capacity.addProperty("minimum", grouping.getMinimumCapacity());
                capacity.addProperty("maximum", grouping.getMaximumCapacity());
                capacity.addProperty("ideal", grouping.getIdealCapacity());
            }));
            data.add(
                    "courses",
                    grouping.getExecutionCourses()
                            .stream()
                            .sorted(ExecutionCourse.EXECUTION_COURSE_NAME_COMPARATOR)
                            .map(getAPISerializer().getExecutionCourseSerializer()::serialize)
                            .collect(StreamUtils.toJsonArray())
            );
            data.add(
                    "groups",
                    grouping.getStudentGroupsSet()
                            .stream()
                            .sorted(StudentGroup.COMPARATOR_BY_GROUP_NUMBER)
                            .map(getAPISerializer().getStudentGroupSerializer()::serialize)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

}
