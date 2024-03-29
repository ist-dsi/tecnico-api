package pt.ist.tecnicoapi.serializer;

import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.academic.domain.StudentGroup;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class StudentGroupSerializer extends DomainObjectSerializer {

    protected StudentGroupSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull StudentGroup studentGroup) {
        return JsonUtils.toJson(data -> {
            data.addProperty("groupNumber", studentGroup.getGroupNumber());
            addIfAndFormatElement(
                    data,
                    "shift",
                    studentGroup.getShift(),
                    getAPISerializer().getShiftSerializer()::serialize
            );
            data.add(
                    "members",
                    studentGroup.getAttendsSet()
                            .stream()
                            .sorted(Attends.COMPARATOR_BY_STUDENT_NUMBER)
                            .map(getAPISerializer().getAttendsSerializer()::serialize)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

}
