package pt.ist.tecnicoapi.serializer;

import org.fenixedu.academic.domain.LessonInstance;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.spaces.domain.Space;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

import java.util.Optional;

public class LessonInstanceSerializer extends DomainObjectSerializer {

    protected LessonInstanceSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull LessonInstance lessonInstance) {
        final Space room = Optional.ofNullable(lessonInstance.getRoom())
                .orElseGet(() -> lessonInstance.getLesson().getSala());
        return JsonUtils.toJson(data -> {
            data.addProperty("start", lessonInstance.getBeginDateTime().toString());
            data.addProperty("end", lessonInstance.getEndDateTime().toString());
            addIfAndFormatElement(data, "room", room, getAPISerializer().getSpaceSerializer()::serializeBasic);
        });
    }

}
