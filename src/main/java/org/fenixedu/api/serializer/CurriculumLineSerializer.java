package org.fenixedu.api.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.studentCurriculum.Credits;
import org.fenixedu.academic.domain.studentCurriculum.CurriculumLine;
import org.fenixedu.academic.domain.studentCurriculum.Dismissal;
import org.fenixedu.academic.domain.studentCurriculum.Equivalence;
import org.fenixedu.academic.domain.studentCurriculum.ExternalEnrolment;
import org.fenixedu.academic.domain.studentCurriculum.InternalSubstitution;
import org.fenixedu.academic.domain.studentCurriculum.Substitution;
import org.fenixedu.academic.domain.studentCurriculum.TemporarySubstitution;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CurriculumLineSerializer extends DomainObjectSerializer {

    protected CurriculumLineSerializer(@NotNull FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull CurriculumLine curriculumEntry) {
        return JsonUtils.toJson(data -> {
            data.add("name", curriculumEntry.getPresentationName().json());
            data.addProperty("ects", curriculumEntry.getEctsCreditsForCurriculum());
            data.add(
                    "semester",
                    getAPISerializer().getExecutionSemesterSerializer()
                            .serializeExtended(curriculumEntry.getExecutionPeriod())
            );
            if (curriculumEntry instanceof Enrolment) {
                Enrolment enrolment = (Enrolment) curriculumEntry;
                CurricularCourse course = enrolment.getCurricularCourse();
                data.addProperty("type", "ENROLMENT");
                data.addProperty("grade", enrolment.getGradeValue());
                addIfAndFormatElement(
                        data,
                        "curricularCourse",
                        course,
                        c -> getAPISerializer().getCurricularCourseSerializer()
                                .serializeWithoutExecutions(c, curriculumEntry.getExecutionPeriod())
                );
                addIfAndFormatElement(
                        data,
                        "executionCourse",
                        enrolment.getExecutionCourseFor(curriculumEntry.getExecutionPeriod()),
                        getAPISerializer().getExecutionCourseSerializer()::serialize
                );
                data.add(
                        "state",
                        getAPISerializer().getEnrolmentSerializer()
                                .serializeEnrolmentState(enrolment.getEnrollmentState())
                );
                data.add(
                        "evaluationSeason",
                        getAPISerializer().getEvaluationSeasonSerializer().serialize(enrolment.getEvaluationSeason())
                );
            } else if (curriculumEntry instanceof Dismissal) {
                Dismissal dismissal = (Dismissal) curriculumEntry;
                CurricularCourse course = dismissal.getCurricularCourse();
                data.addProperty("type", "DISMISSAL");
                data.addProperty("grade", dismissal.getGradeValue());
                data.add("dismissalType", this.serializeCreditsType(dismissal.getCredits()));
                addIfAndFormatElement(
                        data,
                        "curricularCourse",
                        course,
                        c -> getAPISerializer().getCurricularCourseSerializer()
                                .serializeWithoutExecutions(c, curriculumEntry.getExecutionPeriod())
                );
                addIfAndFormatElement(
                        data,
                        "executionCourse",
                        course == null
                                ? null
                                : course.getExecutionCoursesByExecutionPeriod(curriculumEntry.getExecutionPeriod())
                                        .stream()
                                        .findAny()
                                        .orElse(null),
                        getAPISerializer().getExecutionCourseSerializer()::serialize
                );
                data.add("sourceEnrolments", dismissal.getSourceIEnrolments().stream().map(iEnrolment -> {
                    if (iEnrolment instanceof Enrolment) {
                        JsonObject enrolmentData = getAPISerializer().getEnrolmentSerializer()
                                .serialize((Enrolment) iEnrolment);
                        enrolmentData.addProperty("type", "ENROLMENT");
                        return enrolmentData;
                    }
                    if (iEnrolment instanceof ExternalEnrolment) {
                        JsonObject enrolmentData = getAPISerializer().getEnrolmentSerializer()
                                .serializeExternalWithSemester((ExternalEnrolment) iEnrolment);
                        enrolmentData.addProperty("type", "EXTERNAL_ENROLMENT");
                        return enrolmentData;
                    }
                    return null;
                })
                        .filter(Objects::nonNull)
                        .collect(StreamUtils.toJsonArray()));
            } else {
                data.addProperty("type", "OTHER");
            }
        });
    }

    public @NotNull JsonElement serializeCreditsType(@NotNull Credits credits) {
        if (credits instanceof InternalSubstitution) {
            return new JsonPrimitive("INTERNAL_SUBSTITUTION");
        }
        if (credits instanceof TemporarySubstitution) {
            return new JsonPrimitive("TEMPORARY_SUBSTITUTION");
        }
        if (credits instanceof Substitution) {
            return new JsonPrimitive("SUBSTITUTION");
        }
        if (credits instanceof Equivalence) {
            return new JsonPrimitive("EQUIVALENCE");
        }
        return new JsonPrimitive("CREDITS");
    }

}
