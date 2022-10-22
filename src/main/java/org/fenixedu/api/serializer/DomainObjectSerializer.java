package org.fenixedu.api.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class DomainObjectSerializer {

    private final @NotNull FenixEduAPISerializer apiSerializer;

    protected DomainObjectSerializer(@NotNull FenixEduAPISerializer apiSerializer) {
        this.apiSerializer = apiSerializer;
    }

    protected @NotNull FenixEduAPISerializer getAPISerializer() {
        return apiSerializer;
    }

    protected <T> void addIfAndFormat(@NotNull JsonObject object,
                                      @NotNull String key,
                                      @Nullable T value,
                                      @NotNull Function<@NotNull T, @Nullable String> format) {
        if (value != null) {
            String formattedValue = format.apply(value);
            if (formattedValue != null) {
                object.addProperty(key, formattedValue);
            }
        }
    }

    protected <T> void addIfAndFormatElement(@NotNull JsonObject object,
                                             @NotNull String key,
                                             @Nullable T value,
                                             Function<@NotNull T, @Nullable JsonElement> format) {
        if (value != null) {
            JsonElement formattedValue = format.apply(value);
            if (formattedValue != null) {
                object.add(key, formattedValue);
            }
        }
    }
}
