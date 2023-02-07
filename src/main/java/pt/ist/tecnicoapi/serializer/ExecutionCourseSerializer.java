package pt.ist.tecnicoapi.serializer;

import java.util.Collection;

import org.fenixedu.academic.domain.CompetenceCourse;
import org.fenixedu.academic.domain.ExecutionCourse;
import org.fenixedu.academic.domain.Professorship;
import org.fenixedu.academic.domain.degreeStructure.BibliographicReferences;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

public class ExecutionCourseSerializer extends DomainObjectSerializer {

    protected ExecutionCourseSerializer(TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull ExecutionCourse executionCourse) {
        return JsonUtils.toJson(data -> {
            data.addProperty("id", executionCourse.getExternalId());
            data.addProperty("acronym", executionCourse.getSigla());
            data.add("name", executionCourse.getNameI18N().json());
            data.add(
                    "academicTerm",
                    this.getAPISerializer()
                            .getExecutionSemesterSerializer()
                            .serializeExtended(executionCourse.getExecutionPeriod())
            );
            data.add("courseInformation", JsonUtils.toJson(info -> info.add("urls", JsonUtils.toJson(urls -> {
                final String url = executionCourse.getSiteUrl();
                urls.addProperty("courseUrl", url);
                urls.addProperty("rssAnnouncementsUrl", url.concat("/rss/announcement"));
                urls.addProperty("rssSummariesUrl", url.concat("/rss/summary"));
            }))));
        });
    }

    public @NotNull JsonObject serializeExtended(@NotNull ExecutionCourse executionCourse) {
        JsonObject data = serialize(executionCourse);
        JsonObject courseInformation = data.getAsJsonObject("courseInformation");
        courseInformation.add(
                "bibliography",
                executionCourse.getCompetenceCourses()
                        .stream()
                        .map(CompetenceCourse::getBibliographicReferences)
                        .map(BibliographicReferences::getBibliographicReferencesSortedByOrder)
                        .flatMap(Collection::stream)
                        .map(this.getAPISerializer().getBibliographicReferenceSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        courseInformation.add(
                "degrees",
                executionCourse.getDegreesSortedByDegreeName()
                        .stream()
                        .map(this.getAPISerializer().getDegreeSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        // a non-public version of this could probably return the actual list of enrolled students?
        courseInformation.addProperty("enrolledCount", executionCourse.getTotalEnrolmentStudentNumber());
        courseInformation.addProperty("attendingCount", executionCourse.getAttendsSet().size());
        courseInformation.addProperty("evaluationMethods", executionCourse.getLocalizedEvaluationMethodText());
        courseInformation.add(
                "teachers",
                executionCourse.getProfessorshipsSet()
                        .stream()
                        .map(Professorship::getTeacher)
                        .map(this.getAPISerializer().getTeacherSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        data.add("courseInformation", courseInformation);
        return data;
    }
}
