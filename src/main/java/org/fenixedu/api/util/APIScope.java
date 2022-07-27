package org.fenixedu.api.util;

public enum APIScope {

    PERSONAL_READ("read:personal"),
    STUDENT_READ("read:student"),
    TEACHER_READ("read:teacher"),
    EVALUATIONS_READ("read:evaluations"),
    EVALUATIONS_WRITE("write:evaluations");

    private final String key;

    APIScope(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
