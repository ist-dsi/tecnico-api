package org.fenixedu.api.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.curriculum.EnrollmentState;
import org.fenixedu.academic.domain.studentCurriculum.ExternalEnrolment;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

public class EnrolmentSerializer extends DomainObjectSerializer {

    protected EnrolmentSerializer(@NotNull FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Enrolment enrolment) {
        return this.serialize(enrolment, enrolment.getExecutionPeriod());
    }

    public @NotNull JsonObject serialize(@NotNull Enrolment enrolment,
                                         @NotNull ExecutionSemester executionSemester) {
        return JsonUtils.toJson(data -> {
            data.addProperty("grade", enrolment.getGradeValue());
            data.addProperty("ects", enrolment.getEctsCredits());
            addIfAndFormatElement(
                    data,
                    "curricularCourse",
                    enrolment.getCurricularCourse(),
                    c -> getAPISerializer().getCurricularCourseSerializer()
                            .serializeWithoutExecutions(c, executionSemester)
            );
            addIfAndFormatElement(
                    data,
                    "executionCourse",
                    enrolment.getExecutionCourseFor(executionSemester),
                    getAPISerializer().getExecutionCourseSerializer()::serialize
            );
            data.add("state", serializeEnrolmentState(enrolment.getEnrollmentState()));
        });
    }

    public @NotNull JsonObject serializeExternalWithSemester(@NotNull ExternalEnrolment enrolment) {
        return JsonUtils.toJson(data -> {
            data.addProperty("grade", enrolment.getGradeValue());
            data.addProperty("ects", enrolment.getEctsCredits());
            data.addProperty(
                    "course",
                    enrolment.getDescription()
            );
            data.add(
                    "semester",
                    getAPISerializer().getExecutionSemesterSerializer()
                            .serializeExtended(enrolment.getExecutionPeriod())
            );
        });
    }

    public @NotNull JsonElement serializeEnrolmentState(@NotNull EnrollmentState state) {
        // custom serializer since the enum has typos
        switch (state) {
            case APROVED:
                return new JsonPrimitive("APPROVED");
            case NOT_APROVED:
                return new JsonPrimitive("NOT_APPROVED");
            case ENROLLED:
                return new JsonPrimitive("ENROLLED");
            case TEMPORARILY_ENROLLED:
                return new JsonPrimitive("TEMPORARILY_ENROLLED");
            case ANNULED:
                return new JsonPrimitive("ANNULLED");
            case NOT_EVALUATED:
                return new JsonPrimitive("NOT_EVALUATED");
            default:
                return new JsonPrimitive("UNKNOWN"); // unreachable
        }
    }
}
