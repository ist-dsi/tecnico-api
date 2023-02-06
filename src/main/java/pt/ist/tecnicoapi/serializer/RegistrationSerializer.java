package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.curriculum.ICurriculum;
import org.fenixedu.academic.domain.student.registrationStates.RegistrationStateType;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;

public class RegistrationSerializer extends DomainObjectSerializer {

    protected RegistrationSerializer(@NotNull TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Registration registration) {
        final Degree degree = registration.getDegree();

        return JsonUtils.toJson(data -> {
            data.add("degree", this.getAPISerializer().getDegreeSerializer().serialize(degree));

            data.addProperty("studentNumber", registration.getNumber());
            data.add("state", this.serializeRegistrationStateType(registration.getActiveStateType()));

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
        data.addProperty("gradeAverage", curriculum.getRawGrade().getNumericValue());
        data.addProperty("roundedGradeAverage", curriculum.getFinalGrade().getIntegerValue());
        data.addProperty("isConcluded", registration.isConcluded());

        data.add(
                "curricularPlans",
                registration.getStudentCurricularPlanStream()
                        .map(this::serializeStudentCurricularPlan)
                        .collect(StreamUtils.toJsonArray())
        );

        return data;
    }

    public @NotNull JsonObject serializeStudentCurricularPlan(@NotNull StudentCurricularPlan curricularPlan) {
        return JsonUtils.toJson(data -> {
            data.addProperty("name", curricularPlan.getName());
            data.addProperty("startDate", curricularPlan.getStartDateYearMonthDay().toString());
            addIfAndFormat(data, "endDate", curricularPlan.getEndStageDate(), LocalDate::toString);
            data.add(
                    "entries",
                    curricularPlan.getCurriculumLineStream()
                            .map(getAPISerializer().getCurriculumLineSerializer()::serialize)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

    public @NotNull JsonObject serializeForOthers(@NotNull Registration registration) {
        final Person person = registration.getPerson();
        final Degree degree = registration.getDegree();
        return JsonUtils.toJson(data -> {
            data.addProperty("username", person.getUsername());
            data.add("degree", this.getAPISerializer().getDegreeSerializer().serialize(degree));
        });
    }

    public @NotNull JsonElement serializeRegistrationStateType(@NotNull RegistrationStateType stateType) {
        switch (stateType) {
            case REGISTERED:
                return new JsonPrimitive("REGISTERED");
            case MOBILITY:
                return new JsonPrimitive("MOBILITY");
            case CANCELED:
                return new JsonPrimitive("CANCELED");
            case CONCLUDED:
                return new JsonPrimitive("CONCLUDED");
            case FLUNKED:
                return new JsonPrimitive("FLUNKED");
            case INTERRUPTED:
                return new JsonPrimitive("INTERRUPTED");
            case SCHOOLPARTCONCLUDED:
                return new JsonPrimitive("SCHOOL_PART_CONCLUDED");
            case INTERNAL_ABANDON:
                return new JsonPrimitive("INTERNAL_ABANDON");
            case EXTERNAL_ABANDON:
                return new JsonPrimitive("EXTERNAL_ABANDON");
            case TRANSITION:
                return new JsonPrimitive("TRANSITION");
            case TRANSITED:
                return new JsonPrimitive("TRANSITED");
            case STUDYPLANCONCLUDED:
                return new JsonPrimitive("STUDY_PLAN_CONCLUDED");
            case INACTIVE:
                return new JsonPrimitive("INACTIVE");
            default:
                return new JsonPrimitive("UNKNOWN"); // unreachable
        }
    }

    private @NotNull JsonObject toSemesterJson(@NotNull ExecutionSemester semester) {
        return this.getAPISerializer().getExecutionSemesterSerializer().serializeExtended(semester);
    }

}
