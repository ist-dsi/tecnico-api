package pt.ist.tecnicoapi.serializer;

import org.fenixedu.academic.domain.AdHocEvaluation;
import org.fenixedu.academic.domain.Evaluation;
import org.fenixedu.academic.domain.Exam;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.Project;
import org.fenixedu.academic.domain.WrittenEvaluation;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class EvaluationSerializer extends DomainObjectSerializer {

    protected EvaluationSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull Evaluation evaluation) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", evaluation.getExternalId());
            data.addProperty("name", evaluation.getPresentationName());
            data.add(
                    "type",
                    this.getAPISerializer().getEvaluationTypeSerializer().serialize(evaluation.getEvaluationType())
            );
            data.add(
                    "courses",
                    evaluation.getAssociatedExecutionCoursesSet()
                            .stream()
                            .map(this.getAPISerializer().getExecutionCourseSerializer()::serialize)
                            .collect(StreamUtils.toJsonArray())
            );
        });
    }

    public @NotNull JsonObject serializeExtended(@NotNull Evaluation evaluation) {
        if (evaluation instanceof AdHocEvaluation) {
            return serializeExtended((AdHocEvaluation) evaluation);
        } else if (evaluation instanceof Project) {
            return serializeExtended((Project) evaluation);
        } else if (evaluation instanceof WrittenEvaluation) {
            return serializeExtended((WrittenEvaluation) evaluation);
        }
        return serialize(evaluation);
    }

    public @NotNull JsonObject serializeExtended(@NotNull AdHocEvaluation evaluation) {
        JsonObject data = serialize(evaluation);
        data.addProperty("description", evaluation.getDescription());
        return data;
    }

    public @NotNull JsonObject serializeExtended(@NotNull Project project) {
        JsonObject data = serialize(project);
        data.add("evaluationPeriod", JsonUtils.toJson(period -> {
            period.addProperty("start", project.getProjectBeginDateTime().toString());
            period.addProperty("end", project.getProjectEndDateTime().toString());
        }));
        return data;
    }

    public @NotNull JsonObject serializeExtended(@NotNull WrittenEvaluation evaluation) {
        JsonObject data = serialize(evaluation);
        if (evaluation.isExam()) {
            data.add(
                    "season",
                    getAPISerializer().getEvaluationSeasonSerializer().serialize(((Exam) evaluation).getSeason())
            );
        }
        if (evaluation.getEnrolmentPeriodStart() != null && evaluation.getEnrolmentPeriodEnd() != null) {
            data.add("enrolmentPeriod", JsonUtils.toJson(period -> {
                period.addProperty("currentlyOpen", evaluation.isInEnrolmentPeriod());
                period.addProperty("start", evaluation.getEnrolmentPeriodStart().toString());
                period.addProperty("end", evaluation.getEnrolmentPeriodEnd().toString());
            }));
        }
        data.add("evaluationPeriod", JsonUtils.toJson(period -> {
            period.addProperty("start", evaluation.getBeginningDateTime().toString());
            period.addProperty("end", evaluation.getEndDateTime().toString());
        }));
        addIfAndFormatElement(
                data,
                "rooms",
                evaluation.getAssociatedRooms(),
                rooms -> rooms.stream()
                        .map(this.getAPISerializer().getSpaceSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

    public @NotNull JsonObject serializeWithEnrolmentState(@NotNull Evaluation evaluation,
                                                           @NotNull ExecutionCourse executionCourse,
                                                           @NotNull Registration registration) {
        JsonObject data = serializeExtended(evaluation);
        data.add("course", getAPISerializer().getExecutionCourseSerializer().serialize(executionCourse));

        if (evaluation instanceof WrittenEvaluation) {
            boolean isEnroled = ((WrittenEvaluation) evaluation).getWrittenEvaluationEnrolmentFor(registration) != null;
            data.addProperty("isEnroled", isEnroled);
        }

        return data;
    }

}
