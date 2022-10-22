package org.fenixedu.api.serializer;

import org.fenixedu.academic.domain.SchoolClass;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class SchoolClassSerializer extends DomainObjectSerializer {

    protected SchoolClassSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull SchoolClass schoolClass) {
        return JsonUtils.toJson(data -> {
            data.addProperty("name", schoolClass.getNome());
            data.addProperty("curricularYear", schoolClass.getAnoCurricular());
            data.add(
                    "degree",
                    this.getAPISerializer()
                            .getDegreeSerializer()
                            .serialize(schoolClass.getExecutionDegree().getDegree())
            );
        });
    }

}
