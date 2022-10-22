package org.fenixedu.api.serializer;

import org.fenixedu.academic.domain.LessonInstance;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class LessonInstanceSerializer extends DomainObjectSerializer {

    protected LessonInstanceSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull LessonInstance lessonInstance) {
        return JsonUtils.toJson(data -> {
            data.addProperty("start", lessonInstance.getBeginDateTime().toString());
            data.addProperty("end", lessonInstance.getEndDateTime().toString());
            data.add("room", getAPISerializer().getSpaceSerializer().serializeBasic(lessonInstance.getRoom()));
        });
    }

}
