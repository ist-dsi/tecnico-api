package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

public class ExecutionSemesterSerializer extends DomainObjectSerializer {

    protected ExecutionSemesterSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull ExecutionSemester executionSemester) {
        return JsonUtils.toJson(data -> {
            data.addProperty("displayName", executionSemester.getQualifiedName());
            data.addProperty("semester", executionSemester.getSemester());
            data.addProperty("beginDate", executionSemester.getBeginLocalDate().toString());
            data.addProperty("endDate", executionSemester.getEndLocalDate().toString());
        });
    }

    public @NotNull JsonObject serializeExtended(@NotNull ExecutionSemester executionSemester) {
        JsonObject data = serialize(executionSemester);
        addIfAndFormatElement(
                data,
                "year",
                executionSemester.getExecutionYear(),
                year -> this.getAPISerializer().getExecutionYearSerializer().serialize(year)
        );
        return data;
    }
}
