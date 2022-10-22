package org.fenixedu.api.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

public class ExecutionYearSerializer extends DomainObjectSerializer {

    protected ExecutionYearSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull ExecutionYear executionYear) {
        return JsonUtils.toJson(data -> {
            data.addProperty("displayName", executionYear.getQualifiedName());
            data.addProperty("beginYear", executionYear.getBeginCivilYear());
            data.addProperty("endYear", executionYear.getEndCivilYear());
            data.addProperty("beginDate", executionYear.getBeginLocalDate().toString());
            data.addProperty("endDate", executionYear.getEndLocalDate().toString());
        });
    }

    public @NotNull JsonObject serializeExtended(@NotNull ExecutionYear executionYear) {
        JsonObject data = serialize(executionYear);
        data.add(
                "firstSemester",
                this.getAPISerializer()
                        .getExecutionSemesterSerializer()
                        .serialize(executionYear.getFirstExecutionPeriod())
        );
        data.add(
                "secondSemester",
                this.getAPISerializer()
                        .getExecutionSemesterSerializer()
                        .serialize(executionYear.getLastExecutionPeriod())
        );
        return data;
    }
}
