package org.fenixedu.api.serializer;

import org.fenixedu.academic.domain.Lesson;
import org.fenixedu.academic.domain.WrittenEvaluation;
import org.fenixedu.academic.domain.space.LessonInstanceSpaceOccupation;
import org.fenixedu.academic.domain.space.LessonSpaceOccupation;
import org.fenixedu.academic.domain.space.WrittenEvaluationSpaceOccupation;
import org.fenixedu.academic.dto.resourceAllocationManager.OccupationType;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.spaces.domain.occupation.Occupation;
import org.jetbrains.annotations.NotNull;
import org.joda.time.Interval;

import com.google.gson.JsonObject;

public class EventSerializer extends DomainObjectSerializer {

    protected EventSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Occupation occupation, @NotNull Interval interval) {
        return JsonUtils.toJson(event -> {
            event.addProperty("start", interval.getStart().toString());
            event.addProperty("end", interval.getEnd().toString());
            event.addProperty("name", occupation.getSubject());
            if (occupation.getConfig() != null) {
                event.addProperty("description", occupation.getSummary());
                event.addProperty("extendedDescription", occupation.getExtendedSummary());
            }
            addIfAndFormat(event, "url", occupation.getUrl(), url -> url.isEmpty() ? null : url);
            event.addProperty("type", OccupationType.GENERIC.name()); // this might be overwritten by other event types
        });
    }

    public @NotNull JsonObject serializeLesson(@NotNull LessonSpaceOccupation occupation, @NotNull Interval interval) {
        JsonObject event = serialize(occupation, interval);
        final Lesson lesson = occupation.getLesson();
        event.addProperty("type", OccupationType.LESSON.name());
        event.add("shift", getAPISerializer().getShiftSerializer().serialize(lesson.getShift()));
        event.add("course", getAPISerializer().getExecutionCourseSerializer().serialize(lesson.getExecutionCourse()));
        return event;
    }

    public @NotNull JsonObject serializeLessonInstance(@NotNull LessonInstanceSpaceOccupation occupation,
                                                       @NotNull Interval interval) {
        JsonObject event = serialize(occupation, interval);
        // It seems like all lesson instances are of the same lesson, they're just different days
        final Lesson lesson = occupation.getLessonInstancesSet().iterator().next().getLesson();
        event.addProperty("type", OccupationType.LESSON.name());
        event.add("shift", getAPISerializer().getShiftSerializer().serialize(lesson.getShift()));
        event.add("course", getAPISerializer().getExecutionCourseSerializer().serialize(lesson.getExecutionCourse()));
        return event;
    }

    public @NotNull JsonObject serializeWrittenEvaluation(@NotNull WrittenEvaluationSpaceOccupation occupation,
                                                          @NotNull Interval interval) {
        JsonObject event = serialize(occupation, interval);
        // There is always at least an evaluation because of interval checking
        // Apparently, there is almost always only one evaluation in the set, so we can ignore the other ones
        final WrittenEvaluation evaluation = occupation.getWrittenEvaluationsSet().iterator().next();
        event.addProperty("type", OccupationType.EVALUATION.name());
        event.add("evaluation", getAPISerializer().getEvaluationSerializer().serialize(evaluation));
        return event;
    }

}
