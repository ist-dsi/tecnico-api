package org.fenixedu.api.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.degreeStructure.CourseGroup;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

public class CourseGroupSerializer extends DomainObjectSerializer {

    protected CourseGroupSerializer(@NotNull FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull CourseGroup courseGroup) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", courseGroup.getExternalId());
            data.add("name", courseGroup.getNameI18N().json());
        });
    }
}
