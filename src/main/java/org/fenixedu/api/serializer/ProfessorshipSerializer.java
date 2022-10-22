package org.fenixedu.api.serializer;

import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class ProfessorshipSerializer extends DomainObjectSerializer {

    protected ProfessorshipSerializer(FenixEduAPISerializer apiSerializer) {
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

}
