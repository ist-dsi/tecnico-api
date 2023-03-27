package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.Department;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

public class DepartmentSerializer extends DomainObjectSerializer {

    protected DepartmentSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Department department) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", department.getExternalId());
            data.addProperty("acronym", department.getAcronym());
            data.add("name", department.getNameI18n().json());
            data.addProperty("active", department.getActive());
        });
    }

    public @NotNull JsonObject serializeExtended(@NotNull Department department) {
        JsonObject data = serialize(department);
        data.add(
                "currentTeachers",
                department.getAllCurrentTeachers()
                        .stream()
                        .sorted(Teacher.TEACHER_COMPARATOR_BY_CATEGORY_AND_NUMBER)
                        .map(getAPISerializer().getTeacherSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        data.add(
                "currentDegrees",
                department.getDegreesSet()
                        .stream()
                        .sorted(Degree.COMPARATOR_BY_DEGREE_TYPE_DEGREE_NAME_AND_ID)
                        .map(getAPISerializer().getDegreeSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }
}
