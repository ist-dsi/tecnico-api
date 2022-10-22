package org.fenixedu.api.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.degreeStructure.ProgramConclusion;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.curriculum.ICurriculum;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;

import java.util.Optional;

public class RegistrationSerializer extends DomainObjectSerializer {

    protected RegistrationSerializer(@NotNull FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Registration registration) {
        final Degree degree = registration.getDegree();

        return JsonUtils.toJson(data -> {
            data.add("degreeName", degree.getNameI18N().json());
            data.addProperty("degreeAcronym", degree.getSigla());
            data.add("degreeType", degree.getDegreeType().getName().json());
            data.addProperty("degreeId", degree.getExternalId());

            final JsonArray academicTerms = registration.getEnrolmentsExecutionPeriods()
                    .stream()
                    .sorted(ExecutionSemester.COMPARATOR_BY_SEMESTER_AND_YEAR)
                    .map(this::toSemesterJson)
                    .collect(StreamUtils.toJsonArray());
            data.add("academicTerms", academicTerms);
        });
    }

    public @NotNull JsonObject serializeWithCurriculumInformation(@NotNull Registration registration) {
        final JsonObject data = serialize(registration);

        final StudentCurricularPlan lastCurricularPlan = registration.getLastStudentCurricularPlan();

        data.addProperty(
                "startDate",
                registration.getFirstStudentCurricularPlan().getStartDateYearMonthDay().toString()
        );
        addIfAndFormat(data, "endDate", lastCurricularPlan.getEndDate(), YearMonthDay::toString);

        final ICurriculum curriculum = lastCurricularPlan.getCurriculum(new DateTime(), null);

        data.addProperty("curricularYear", curriculum.getCurricularYear());
        data.addProperty("credits", curriculum.getSumEctsCredits());
        data.addProperty("average", curriculum.getRawGrade().getNumericValue());
        data.addProperty("roundedAverage", curriculum.getFinalGrade().getIntegerValue());
        data.addProperty("isConcluded", registration.isConcluded());

        data.add(
                "approvedCourses",
                registration.getStudentCurricularPlanStream()
                        // Get all groups for conclusion
                        .flatMap(
                                curricularPlan -> ProgramConclusion.conclusionsFor(curricularPlan)
                                        .map(programConclusion -> programConclusion.groupFor(curricularPlan))
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                        )
                        .flatMap(curriculumGroup -> curriculumGroup.getCurriculum().getCurriculumEntries().stream())
                        .map(getAPISerializer().getICurriculumEntrySerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );

        return data;
    }

    private @NotNull JsonObject toSemesterJson(@NotNull ExecutionSemester semester) {
        return this.getAPISerializer().getExecutionSemesterSerializer().serializeExtended(semester);
    }

}
