package pt.ist.tecnicoapi.serializer;

import org.fenixedu.academic.domain.util.icalendar.ClassEventBean;
import org.fenixedu.academic.domain.util.icalendar.EvaluationEventBean;
import org.fenixedu.academic.domain.util.icalendar.EventBean;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class EventBeanSerializer extends DomainObjectSerializer {

    protected EventBeanSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull EventBean event) {
        if (event instanceof ClassEventBean) {
            return serializeClassEvent((ClassEventBean) event);
        } else if (event instanceof EvaluationEventBean) {
            return serializeEvaluationEvent((EvaluationEventBean) event);
        }
        return serializeGenericEvent(event);
    }

    public @NotNull JsonObject serializeGenericEvent(@NotNull EventBean event) {
        return JsonUtils.toJson(data -> {
            data.addProperty("start", event.getBegin().toString());
            data.addProperty("end", event.getBegin().toString());

            data.add("title", fakeLocalizedString(event.getOriginalTitle()).json());

            addIfAndFormatElement(
                    data,
                    "locations",
                    event.getRooms(),
                    rooms -> rooms.stream()
                            .map(this.getAPISerializer().getSpaceSerializer()::serializeBasic)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

    public @NotNull JsonObject serializeClassEvent(@NotNull ClassEventBean event) {
        JsonObject data = serializeGenericEvent(event);
        data.add("shift", this.getAPISerializer().getShiftSerializer().serialize(event.getClassShift()));
        data.add(
                "course",
                this.getAPISerializer()
                        .getExecutionCourseSerializer()
                        .serialize(event.getClassShift().getExecutionCourse())
        );

        return data;
    }

    public @NotNull JsonObject serializeEvaluationEvent(@NotNull EvaluationEventBean event) {
        JsonObject data = serializeGenericEvent(event);
        this.addIfAndFormatElement(
                data,
                "assignedRoom",
                event.getAssignedRoom(),
                this.getAPISerializer().getSpaceSerializer()::serializeBasic
        );
        data.add(
                "courses",
                event.getCourses()
                        .stream()
                        .map(this.getAPISerializer().getExecutionCourseSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );

        return data;
    }

    /**
     * EventBean title's is not a {@link LocalizedString}.
     * While that is not fixed, this will create a localized string with the same text across all languages.
     * This method SHOULD and WILL be removed in the future.
     *
     * @param string The string to replicate on all languages.
     * @return A LocalizedString with the same text for all supported languages.
     */
    private @NotNull LocalizedString fakeLocalizedString(@NotNull String string) {
        return CoreConfiguration.supportedLocales()
                .stream()
                .reduce(new LocalizedString.Builder(), (ls, locale) -> ls.with(locale, string), (ls1, ls2) -> ls1)
                .build();
    }

}
