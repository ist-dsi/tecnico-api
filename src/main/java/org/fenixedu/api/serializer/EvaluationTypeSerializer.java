package org.fenixedu.api.serializer;

import org.fenixedu.academic.util.EvaluationType;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonPrimitive;

public class EvaluationTypeSerializer extends DomainObjectSerializer {

    protected EvaluationTypeSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonPrimitive serialize(@NotNull EvaluationType evaluationType) {
        switch (evaluationType.getType()) {
            case 1:
                return new JsonPrimitive("EXAM");
            case 2:
                return new JsonPrimitive("FINAL");
            case 3:
                return new JsonPrimitive("ONLINE_TEST");
            case 4:
                return new JsonPrimitive("TEST");
            case 5:
                return new JsonPrimitive("PROJECT");
            case 6:
                return new JsonPrimitive("AD_HOC");
            default:
                return new JsonPrimitive("UNKNOWN");
        }
    }

}
