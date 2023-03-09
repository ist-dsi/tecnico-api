package pt.ist.tecnicoapi.serializer;

import org.fenixedu.bennu.core.domain.UserProfile;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.cms.domain.Post;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

import java.util.Optional;

public class PostSerializer extends DomainObjectSerializer {

    protected PostSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Post post) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", post.getExternalId());
            data.add("title", post.getName().json());
            data.add("body", post.getBody().json());
            data.addProperty("url", post.getAddress());
            data.addProperty("creationDate", post.getCreationDate().toString());
            data.addProperty("modificationDate", post.getModificationDate().toString());
            addIfAndFormatElement(
                    data,
                    "author",
                    Optional.ofNullable(post.getCreatedBy().getProfile()).map(UserProfile::getPerson).orElse(null),
                    getAPISerializer().getPersonSerializer()::serializeBasic
            );
        });
    }

}
