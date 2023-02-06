package pt.ist.tecnicoapi.serializer;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class TeacherSerializer extends DomainObjectSerializer {

    protected TeacherSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Teacher teacher) {
        final Person person = teacher.getPerson();

        return JsonUtils.toJson(data -> {
            data.addProperty("username", teacher.getTeacherId());
            data.addProperty("name", person.getName());
            // FIXME: the old API returned a list of mails - is one enough (same for web addresses)?
            if (person.hasDefaultEmailAddress()) {
                data.addProperty("emailAddress", person.getDefaultEmailAddressValue());
            }
            if (person.hasDefaultWebAddress()) {
                data.addProperty("webAddress", person.getDefaultWebAddressUrl());
            }
        });
    }

}
