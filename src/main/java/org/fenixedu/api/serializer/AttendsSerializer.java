package org.fenixedu.api.serializer;

import org.fenixedu.academic.domain.Attends;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class AttendsSerializer extends DomainObjectSerializer {

    protected AttendsSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Attends attends) {
        final Registration registration = attends.getRegistration();
        final Person person = registration.getPerson();
        final Degree degree = registration.getDegree();
        return JsonUtils.toJson(data -> {
            data.addProperty("username", person.getUsername());
            data.add("degree", this.getAPISerializer().getDegreeSerializer().serialize(degree));
        });
    }

}
