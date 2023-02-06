package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonPrimitive;
import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class AttendsSerializer extends DomainObjectSerializer {

    protected AttendsSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Attends attends) {
        final ExecutionCourse course = attends.getExecutionCourse();
        return JsonUtils.toJson(data -> {
            data.add("state", serializeAttendsState(attends.getAttendsStateType()));
            data.addProperty("isEnroled", attends.getEnrolment() != null);
            data.add("course", this.getAPISerializer().getExecutionCourseSerializer().serialize(course));
        });
    }

    public @NotNull JsonPrimitive serializeAttendsState(@NotNull Attends.StudentAttendsStateType attendsState) {
        switch (attendsState) {
            case ENROLED:
                return new JsonPrimitive("ENROLED");
            case NOT_ENROLED:
                return new JsonPrimitive("NOT_ENROLED");
            case IMPROVEMENT:
                return new JsonPrimitive("IMPROVEMENT");
            case SPECIAL_SEASON:
                return new JsonPrimitive("SPECIAL_SEASON");
            case EXTRAORDINARY_SEASON:
                return new JsonPrimitive("EXTRAORDINARY_SEASON");
            default:
                return new JsonPrimitive("EXAM");
        }
    }

}
