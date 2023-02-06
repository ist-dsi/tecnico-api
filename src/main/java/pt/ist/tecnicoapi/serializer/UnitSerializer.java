package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

public class UnitSerializer extends DomainObjectSerializer {

    public UnitSerializer(@NotNull TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Unit unit) {
        return JsonUtils.toJson(data -> {
            data.add("name", unit.getNameI18n().json());
            data.addProperty("acronym", unit.getAcronym());
            if (unit.hasDefaultWebAddress()) {
                data.addProperty("url", unit.getDefaultWebAddressUrl());
            }
            if (unit.hasDefaultEmailAddress()) {
                data.addProperty("email", unit.getDefaultEmailAddressValue());
            }
            if (unit.hasDefaultMobilePhone()) {
                data.addProperty("phone", unit.getDefaultMobilePhoneNumber());
            }
        });
    }
}
