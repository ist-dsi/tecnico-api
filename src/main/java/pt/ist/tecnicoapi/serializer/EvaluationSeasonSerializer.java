package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonPrimitive;
import org.fenixedu.academic.domain.EvaluationSeason;
import org.fenixedu.academic.util.Season;
import org.jetbrains.annotations.NotNull;

public class EvaluationSeasonSerializer extends DomainObjectSerializer {

    protected EvaluationSeasonSerializer(TecnicoAPISerializer apiSerializer) {
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

    public @NotNull JsonPrimitive serialize(@NotNull EvaluationSeason evaluationSeason) {
        if (evaluationSeason.isNormal()) {
            return new JsonPrimitive("NORMAL");
        }
        if (evaluationSeason.isImprovement()) {
            return new JsonPrimitive("IMPROVEMENT");
        }
        if (evaluationSeason.isSpecialAuthorization()) {
            return new JsonPrimitive("SPECIAL_AUTHORIZATION");
        }
        if (evaluationSeason.isSpecial()) {
            return new JsonPrimitive("SPECIAL");
        }
        if (evaluationSeason.isExtraordinary()) {
            return new JsonPrimitive("EXTRAORDINARY");
        }
        return new JsonPrimitive("UNKNOWN");
    }

}
