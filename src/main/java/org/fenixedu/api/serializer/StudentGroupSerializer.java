package org.fenixedu.api.serializer;

import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.academic.domain.StudentGroup;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class StudentGroupSerializer extends DomainObjectSerializer {

    protected StudentGroupSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull StudentGroup studentGroup) {
        return JsonUtils.toJson(data -> {
            data.addProperty("groupNumber", studentGroup.getGroupNumber());
            addIfAndFormat(data, "shift", studentGroup.getShift(), Shift::getPresentationName);
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
