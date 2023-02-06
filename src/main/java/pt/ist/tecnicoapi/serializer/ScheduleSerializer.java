package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.Shift;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

public class ScheduleSerializer extends DomainObjectSerializer {

    protected ScheduleSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull ExecutionCourse executionCourse) {
        return JsonUtils.toJson(data -> {
            // data.add(
            //         "lessonPeriods",
            //         executionCourse.getLessonPeriods()
            //                 .stream()
            //                 .map(OccupationPeriod::getIntervals)
            //                 .flatMap(Collection::stream)
            //                 .map(this::toIntervalJson)
            //                 .collect(StreamUtils.toJsonArray())
            // ); FIXME: This is not working - issue already found in V1
            data.add(
                    "courseLoads",
                    executionCourse.getCourseLoadsSet()
                            .stream()
                            .map(this.getAPISerializer().getCourseLoadSerializer()::serialize)
                            .collect(StreamUtils.toJsonArray())
            );
            data.add(
                    "shifts",
                    executionCourse.getAssociatedShifts()
                            .stream()
                            .sorted(Shift.SHIFT_COMPARATOR_BY_NAME)
                            .map(this.getAPISerializer().getShiftSerializer()::serializeExtended)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

}
