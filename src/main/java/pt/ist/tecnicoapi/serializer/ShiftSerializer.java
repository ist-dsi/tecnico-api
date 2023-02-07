package pt.ist.tecnicoapi.serializer;

import java.util.Comparator;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Lesson;
import org.fenixedu.academic.domain.SchoolClass;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.academic.domain.ShiftType;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ShiftSerializer extends DomainObjectSerializer {

    protected ShiftSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Shift shift) {
        return JsonUtils.toJson(data -> {
            data.addProperty("name", shift.getNome());
            data.add(
                    "types",
                    shift.getTypes()
                            .stream()
                            .map(this::serializeShiftType)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

    public @NotNull JsonObject serializeExtended(@NotNull Shift shift) {
        JsonObject data = serialize(shift);
        data.add("enrolments", JsonUtils.toJson(capacity -> {
            capacity.addProperty("maximum", shift.getLotacao());
            capacity.addProperty("current", shift.getStudentsSet().size());
        }));
        data.add(
                "classes",
                shift.getAssociatedClassesSet()
                        .stream()
                        .sorted(SchoolClass.COMPARATOR_BY_NAME)
                        .map(this.getAPISerializer().getSchoolClassSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        data.add(
                "lessons",
                shift.getAssociatedLessonsSet()
                        .stream()
                        .flatMap(lesson -> Stream.concat(getShiftLessonInstances(lesson), getShiftLessons(lesson)))
                        .sorted(Comparator.comparing(lesson -> lesson.get("start").getAsString()))
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

    public @NotNull JsonPrimitive serializeShiftType(@NotNull ShiftType shiftType) {
        switch (shiftType) {
            case TEORICA:
                return new JsonPrimitive("THEORETICAL");
            case PRATICA:
                return new JsonPrimitive("PRACTICAL");
            case TEORICO_PRATICA:
                return new JsonPrimitive("THEORETICAL_PRACTICAL");
            case LABORATORIAL:
                return new JsonPrimitive("LABORATORY");
            case DUVIDAS:
                return new JsonPrimitive("OFFICE_HOURS");
            case RESERVA:
                return new JsonPrimitive("RESERVE");
            // other cases are already in english
            default:
                return new JsonPrimitive(shiftType.getName());
        }
    }

    private Stream<JsonObject> getShiftLessonInstances(Lesson lesson) {
        return lesson.getLessonInstancesSet()
                .stream()
                .map(this.getAPISerializer().getLessonInstanceSerializer()::serialize);
    }

    private Stream<JsonObject> getShiftLessons(Lesson lesson) {
        return lesson.getAllLessonIntervalsWithoutInstanceDates()
                .stream()
                .map(interval -> this.getAPISerializer().getLessonSerializer().serialize(lesson, interval));
    }

}
