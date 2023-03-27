package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.degreeStructure.CompetenceCourseInformation;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

public class CompetenceCourseSerializer extends DomainObjectSerializer {

    protected CompetenceCourseSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull CompetenceCourse competenceCourse) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", competenceCourse.getExternalId());
            data.addProperty("name", competenceCourse.getName());
            data.addProperty("acronym", competenceCourse.getAcronym());
        });
    }

    public @NotNull JsonObject serializeExtended(@NotNull CompetenceCourse competenceCourse,
                                                 @NotNull ExecutionSemester executionSemester) {
        JsonObject data = serialize(competenceCourse);
        final CompetenceCourseInformation competenceCourseInformation = competenceCourse
                .findCompetenceCourseInformationForExecutionPeriod(executionSemester);

        data.add(
                "bibliographicReferences",
                competenceCourseInformation
                        .getBibliographicReferences()
                        .getBibliographicReferencesSortedByOrder()
                        .stream()
                        .map(getAPISerializer().getBibliographicReferenceSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        data.addProperty("regime", competenceCourseInformation.getRegime().getLocalizedName());
        data.add(
                "objectives",
                JsonUtils.toJson(objective -> {
                    objective.addProperty("pt-PT", competenceCourseInformation.getObjectivesEn());
                    objective.addProperty("en-GB", competenceCourseInformation.getObjectivesEn());
                }
                )
        );
        data.add(
                "program",
                JsonUtils.toJson(program -> {
                    program.addProperty("pt-PT", competenceCourseInformation.getProgramEn());
                    program.addProperty("en-GB", competenceCourseInformation.getProgramEn());
                }
                )
        );
        return data;
    }

}
