package org.fenixedu.api.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

public class EnrolmentSerializer extends DomainObjectSerializer {

    protected EnrolmentSerializer(@NotNull FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Enrolment enrolment,
                                         @NotNull ExecutionSemester executionSemester) {
        return JsonUtils.toJson(data -> {
            data.addProperty("grade", enrolment.getGradeValue());
            data.addProperty("ects", enrolment.getEctsCredits());
            data.add(
                    "course",
                    getAPISerializer().getExecutionCourseSerializer()
                            .serialize(enrolment.getExecutionCourseFor(executionSemester))
            );
        });
    }
}
