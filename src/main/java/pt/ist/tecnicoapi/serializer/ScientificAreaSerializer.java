package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.organizationalStructure.ScientificAreaUnit;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

public class ScientificAreaSerializer extends DomainObjectSerializer {

    protected ScientificAreaSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull ScientificAreaUnit scientificAreaUnit) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", scientificAreaUnit.getExternalId());
            data.addProperty("name", scientificAreaUnit.getName());
            data.addProperty("acronym", scientificAreaUnit.getAcronym());
            // TODO: add courses
        });
    }

}
