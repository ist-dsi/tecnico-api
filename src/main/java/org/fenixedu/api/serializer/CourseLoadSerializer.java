package org.fenixedu.api.serializer;

import org.fenixedu.academic.domain.CourseLoad;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class CourseLoadSerializer extends DomainObjectSerializer {

    protected CourseLoadSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull CourseLoad courseLoad) {
        return JsonUtils.toJson(data -> {
            data.addProperty("type", courseLoad.getType().name());
            data.addProperty("weeklyHours", courseLoad.getWeeklyHours());
            data.addProperty("totalHours", courseLoad.getTotalQuantity());
        });
    }

}
