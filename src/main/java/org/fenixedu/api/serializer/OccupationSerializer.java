package org.fenixedu.api.serializer;

import java.util.stream.Stream;

import org.fenixedu.academic.domain.space.EventSpaceOccupation;
import org.fenixedu.academic.domain.space.LessonInstanceSpaceOccupation;
import org.fenixedu.academic.domain.space.LessonSpaceOccupation;
import org.fenixedu.academic.domain.space.WrittenEvaluationSpaceOccupation;
import org.fenixedu.spaces.domain.occupation.Occupation;
import org.jetbrains.annotations.NotNull;
import org.joda.time.Interval;

import com.google.gson.JsonObject;

public class OccupationSerializer extends DomainObjectSerializer {

    protected OccupationSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    /**
     * @param occupation the occupation to serialize
     * @param interval   the interval in which to get events;
     *                   events that don't overlap with this interval should be discarded
     * @return a stream of events, as {@link JsonObject JsonObjects}
     */
    public @NotNull Stream<@NotNull JsonObject> serialize(@NotNull Occupation occupation, @NotNull Interval interval) {
        if (occupation instanceof EventSpaceOccupation) {
            return getEventSpaceOccupationEvents((EventSpaceOccupation) occupation, interval);
        }
        return getSpaceGenericEvents(occupation, interval);
    }

    private @NotNull Stream<@NotNull JsonObject> getEventSpaceOccupationEvents(@NotNull EventSpaceOccupation occupation,
                                                                               @NotNull Interval interval) {
        final EventSerializer eventSerializer = getAPISerializer().getEventSerializer();
        return occupation.getEventSpaceOccupationIntervals(interval.getStart(), interval.getEnd())
                .stream()
                .map(eventInterval -> {
                    if (occupation instanceof LessonSpaceOccupation) {
                        return eventSerializer.serializeLesson((LessonSpaceOccupation) occupation, eventInterval);
                    } else if (occupation instanceof LessonInstanceSpaceOccupation) {
                        return eventSerializer.serializeLessonInstance(
                                (LessonInstanceSpaceOccupation) occupation,
                                eventInterval
                        );
                    } else if (occupation instanceof WrittenEvaluationSpaceOccupation) {
                        return eventSerializer.serializeWrittenEvaluation(
                                (WrittenEvaluationSpaceOccupation) occupation,
                                eventInterval
                        );
                    }
                    return eventSerializer.serialize(occupation, eventInterval);
                });
    }

    private @NotNull Stream<@NotNull JsonObject> getSpaceGenericEvents(@NotNull Occupation occupation,
                                                                       @NotNull Interval interval) {
        return occupation.getIntervals()
                .stream()
                .filter(interval::overlaps)
                .map(
                        eventInterval -> getAPISerializer()
                                .getEventSerializer()
                                .serialize(occupation, eventInterval)
                );
    }

}
