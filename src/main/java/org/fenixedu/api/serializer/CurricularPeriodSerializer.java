package org.fenixedu.api.serializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.fenixedu.academic.domain.curricularPeriod.CurricularPeriod;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class CurricularPeriodSerializer extends DomainObjectSerializer {

    protected CurricularPeriodSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull final CurricularPeriod curricularPeriod,
                                         @Nullable final Integer term) {
        return JsonUtils.toJson(data -> {
            // "term" is the half of the semester when the course occurs, or null if it's the whole semester
            addIfAndFormatElement(data, "quarter", term, JsonPrimitive::new);

            CurricularPeriod currentPeriod = curricularPeriod;
            do {
                addIfAndFormatElement(
                        data,
                        currentPeriod.getAcademicPeriod().getName().toLowerCase(Locale.ROOT),
                        currentPeriod.getChildOrder(),
                        JsonPrimitive::new
                );
                currentPeriod = currentPeriod.getParent();
            } while (currentPeriod != null);
        });
    }
}
