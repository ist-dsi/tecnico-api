package pt.ist.tecnicoapi.serializer;

import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class ProfessorshipSerializer extends DomainObjectSerializer {

    protected ProfessorshipSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Professorship professorship) {
        return JsonUtils.toJson(data -> {
            data.addProperty("isResponsibleFor", professorship.isResponsibleFor());
            data.add(
                    "course",
                    getAPISerializer().getExecutionCourseSerializer().serialize(professorship.getExecutionCourse())
            );
        });
    }

    public @NotNull JsonObject serializeWithTeacherInformation(@NotNull Professorship professorship) {
        JsonObject data = getAPISerializer().getTeacherSerializer().serialize(professorship.getTeacher());
        data.addProperty("isResponsibleFor", professorship.isResponsibleFor());
        return data;
    }

}
