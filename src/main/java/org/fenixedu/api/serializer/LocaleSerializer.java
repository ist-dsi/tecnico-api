package org.fenixedu.api.serializer;

import java.util.Locale;

import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class LocaleSerializer extends DomainObjectSerializer {

    protected LocaleSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Locale locale) {
        return JsonUtils.toJson(data -> {
            data.addProperty("locale", locale.toLanguageTag());
            data.addProperty("name", locale.getDisplayName());
        });
    }

}
