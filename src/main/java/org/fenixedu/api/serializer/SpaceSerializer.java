package org.fenixedu.api.serializer;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import org.fenixedu.academic.domain.space.SpaceUtils;
import org.fenixedu.api.util.SpaceType;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.fenixedu.spaces.domain.Space;
import org.fenixedu.spaces.domain.occupation.Occupation;
import org.jetbrains.annotations.NotNull;
import org.joda.time.Interval;

import com.google.gson.JsonObject;

public class SpaceSerializer extends DomainObjectSerializer {

    protected SpaceSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serializeBasic(@NotNull Space space) {
        final Optional<SpaceType> type = SpaceType.getSpaceType(space);
        return JsonUtils.toJson(data -> {
            data.addProperty("id", space.getExternalId());
            data.addProperty("name", space.getName());
            data.addProperty("fullName", space.getFullName());
            data.addProperty("type", type.map(SpaceType::name).orElse("UNKNOWN"));
            data.add("classification", space.getClassification().getName().json());
        });
    }

    public @NotNull JsonObject serialize(@NotNull Space space) {
        JsonObject data = serializeBasic(space);
        if (SpaceUtils.isRoom(space)) {
            data.add("capacity", JsonUtils.toJson(capacity -> {
                capacity.addProperty("regular", space.getAllocatableCapacity());
                capacity.addProperty("exam", SpaceUtils.countAvailableSeatsForExams(space));
            }));
        }
        data.addProperty("description", space.getPresentationName());
        // possibly only add the building field if a building contains the space - with campi and buildings probably not needed
        addIfAndFormatElement(data, "building", SpaceUtils.getSpaceBuilding(space), this::serializeBasic);
        addIfAndFormatElement(data, "campus", SpaceUtils.getSpaceCampus(space), this::serializeBasic);
        addIfAndFormatElement(data, "containedIn", space.getParent(), this::serializeBasic);
        data.add(
                "contains",
                space.getChildren()
                        .stream()
                        .map(this::serializeBasic)
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

    public @NotNull JsonObject serializeExtended(@NotNull Space space, @NotNull Interval interval) {
        JsonObject data = serialize(space);
        final Set<Occupation> occupations = space.getOccupationSet();
        data.add(
                "schedule",
                occupations.stream()
                        .flatMap(
                                occupation -> this.getAPISerializer()
                                        .getOccupationSerializer()
                                        .serialize(occupation, interval)
                        )
                        .sorted(Comparator.comparing(obj -> obj.get("start").getAsString()))
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

}
