package org.fenixedu.api.serializer;

import org.fenixedu.academic.domain.Lesson;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.Interval;

import com.google.gson.JsonObject;

public class LessonSerializer extends DomainObjectSerializer {

    protected LessonSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Lesson lesson, @NotNull Interval interval) {
        return JsonUtils.toJson(data -> {
            data.addProperty("start", interval.getStart().toString());
            data.addProperty("end", interval.getEnd().toString());
            addIfAndFormatElement(
                    data,
                    "room",
                    lesson.getSala(),
                    getAPISerializer().getSpaceSerializer()::serializeBasic
            );
        });
    }

}
