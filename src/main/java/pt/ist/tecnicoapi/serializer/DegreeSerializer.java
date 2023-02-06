package pt.ist.tecnicoapi.serializer;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class DegreeSerializer extends DomainObjectSerializer {

    protected DegreeSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Degree degree) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", degree.getExternalId());
            data.add("name", degree.getNameI18N().json());
            data.addProperty("acronym", degree.getSigla());
            JsonUtils.addIf(data, "url", degree.getSiteUrl());
            addIfAndFormatElement(
                    data,
                    "campi",
                    degree.getCurrentCampus(),
                    campus -> campus.stream()
                            .map(this.getAPISerializer().getSpaceSerializer()::serializeBasic)
                            .collect(StreamUtils.toJsonArray())
            );
            addIfAndFormatElement(
                    data,
                    "degreeType",
                    degree.getDegreeType().getName(),
                    LocalizedString::json
            );
        });
    }

    public @NotNull JsonObject serializeExtended(@NotNull Degree degree) {
        JsonObject data = serialize(degree);
        addIfAndFormatElement(
                data,
                "academicTerms",
                degree.getExecutionDegrees(),
                degrees -> degrees.stream()
                        // sorted in ascending order
                        .sorted(ExecutionDegree.REVERSE_EXECUTION_DEGREE_COMPARATORY_BY_YEAR)
                        .map(ExecutionDegree::getExecutionYear)
                        .map(this.getAPISerializer().getExecutionYearSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

}
