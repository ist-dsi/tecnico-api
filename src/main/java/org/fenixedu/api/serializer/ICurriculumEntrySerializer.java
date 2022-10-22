package org.fenixedu.api.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.CurricularCourse;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.student.curriculum.ICurriculumEntry;
import org.fenixedu.academic.domain.studentCurriculum.Dismissal;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.jetbrains.annotations.NotNull;

public class ICurriculumEntrySerializer extends DomainObjectSerializer {

    protected ICurriculumEntrySerializer(@NotNull FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull ICurriculumEntry curriculumEntry) {
        return JsonUtils.toJson(data -> {
            data.add("name", curriculumEntry.getPresentationName().json());
            data.addProperty("grade", curriculumEntry.getGradeValue());
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
                addIfAndFormatElement(
                        data,
                        "course",
                        course,
                        c -> getAPISerializer().getCurricularCourseSerializer()
                                .serialize(c, curriculumEntry.getExecutionYear())
                );
                addIfAndFormat(
                        data,
                        "url",
                        enrolment.getExecutionCourseFor(curriculumEntry.getExecutionPeriod()),
                        ExecutionCourse::getSiteUrl
                );
            } else if (curriculumEntry instanceof Dismissal) {
                CurricularCourse course = ((Dismissal) curriculumEntry).getCurricularCourse();
                data.addProperty("type", "DISMISSAL");
                addIfAndFormatElement(
                        data,
                        "course",
                        course,
                        c -> getAPISerializer().getCurricularCourseSerializer()
                                .serialize(c, curriculumEntry.getExecutionYear())
                );
            } else {
                data.addProperty("type", "OTHER");
            }
        });
    }
}
