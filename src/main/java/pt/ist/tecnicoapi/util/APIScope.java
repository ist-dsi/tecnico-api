package pt.ist.tecnicoapi.util;

import org.jetbrains.annotations.NotNull;

public enum APIScope {

    PERSONAL_READ("read:personal"),
    STUDENT_READ("read:student"),
    TEACHER_READ("read:teacher"),
    EVALUATIONS_READ("read:evaluations"),
    EVALUATIONS_WRITE("write:evaluations");

    private final @NotNull String key;

    APIScope(@NotNull String key) {
        this.key = key;
    }

    @Override
    public @NotNull String toString() {
        return key;
    }
}
