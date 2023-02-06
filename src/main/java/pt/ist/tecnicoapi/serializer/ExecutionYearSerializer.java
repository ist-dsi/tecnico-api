package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

public class ExecutionYearSerializer extends DomainObjectSerializer {

    protected ExecutionYearSerializer(TecnicoAPISerializer apiSerializer) {
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
                "semesters",
                executionYear.getExecutionPeriodsSet()
                        .stream()
                        .map(this.getAPISerializer().getExecutionSemesterSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }
}
