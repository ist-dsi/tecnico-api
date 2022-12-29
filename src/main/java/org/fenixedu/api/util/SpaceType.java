package org.fenixedu.api.util;

import org.fenixedu.academic.domain.space.SpaceUtils;
import org.fenixedu.spaces.domain.Space;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

public enum SpaceType {

    CAMPUS(SpaceUtils::isCampus),
    BUILDING(SpaceUtils::isBuilding),
    FLOOR(SpaceUtils::isFloor),
    ROOM(SpaceUtils::isRoomSubdivision),
    ROOM_SUBDIVISION(SpaceUtils::isRoom);

    private final Predicate<Space> isOfType;

    SpaceType(Predicate<Space> isOfType) {
        this.isOfType = isOfType;
    }

    public static @NotNull Optional<SpaceType> getSpaceType(@NotNull Space space) {
        return Arrays.stream(values())
                .filter(type -> type.isOfType.test(space))
                .findAny();
    }

}
