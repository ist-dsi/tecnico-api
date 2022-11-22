package org.fenixedu.api.serializer;

import com.google.gson.JsonPrimitive;
import org.fenixedu.academic.domain.EvaluationSeason;
import org.fenixedu.academic.util.EvaluationType;
import org.fenixedu.academic.util.Season;
import org.jetbrains.annotations.NotNull;

public class EvaluationSeasonSerializer extends DomainObjectSerializer {

    protected EvaluationSeasonSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonPrimitive serialize(@NotNull Season evaluationSeason) {
        switch (evaluationSeason.getSeason()) {
            case 1:
                return new JsonPrimitive("FIRST_SEASON");
            case 2:
                return new JsonPrimitive("SECOND_SEASON");
            case 3:
                return new JsonPrimitive("SPECIAL_SEASON");
            case 4:
                return new JsonPrimitive("EXTRAORDINARY_SEASON");
            default:
                return new JsonPrimitive("UNKNOWN");
        }
    }

}
