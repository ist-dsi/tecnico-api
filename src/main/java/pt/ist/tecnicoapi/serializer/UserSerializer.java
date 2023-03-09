package pt.ist.tecnicoapi.serializer;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class UserSerializer extends DomainObjectSerializer {

    protected UserSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull User user) {
        return JsonUtils.toJson(data -> {
            data.addProperty("name", user.getProfile().getDisplayName());
            data.addProperty("username", user.getUsername());
            data.addProperty("email", user.getEmail());
        });
    }

}
