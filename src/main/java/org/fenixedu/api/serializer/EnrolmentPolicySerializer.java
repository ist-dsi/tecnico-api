package org.fenixedu.api.serializer;

import javax.annotation.Nullable;

import org.fenixedu.academic.util.EnrolmentGroupPolicyType;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonPrimitive;

public class EnrolmentPolicySerializer extends DomainObjectSerializer {

    protected EnrolmentPolicySerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @Nullable JsonPrimitive serialize(@NotNull EnrolmentGroupPolicyType policy) {
        switch (policy.getType()) {
            case 1:
                return new JsonPrimitive("ATOMIC");
            case 2:
                return new JsonPrimitive("INDIVIDUAL");
        }
        return null;
    }

}
