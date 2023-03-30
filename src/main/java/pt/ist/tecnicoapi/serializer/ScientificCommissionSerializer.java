package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.ScientificCommission;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

public class ScientificCommissionSerializer extends DomainObjectSerializer {

    protected ScientificCommissionSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull ScientificCommission scientificCommission) {
        final Person person = scientificCommission.getPerson();

        return JsonUtils.toJson(data -> {
            data.addProperty("username", person.getUsername());
            data.addProperty("displayName", person.getDisplayName());
            data.addProperty("email", person.getDefaultEmailAddressValue());
            data.addProperty("isCoordinationTeam", scientificCommission.getHasCoordinator());
            data.addProperty("isContact", scientificCommission.isContact());
        });
    }

}
