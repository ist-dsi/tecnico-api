package org.fenixedu.api.serializer;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.Coordinator;
import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeInfo;
import org.fenixedu.academic.domain.ExecutionDegree;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.commons.i18n.LocalizedString;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

public class ExecutionDegreeSerializer extends DomainObjectSerializer {

    protected ExecutionDegreeSerializer(FenixEduAPISerializer apiSerializer) {
        super(apiSerializer);
    }

    public @NotNull JsonObject serialize(@NotNull ExecutionDegree executionDegree) {
        return this.getAPISerializer().getDegreeSerializer().serializeExtended(executionDegree.getDegree());
    }

    public @NotNull JsonObject serializeExtended(@NotNull ExecutionDegree executionDegree) {
        final Degree degree = executionDegree.getDegree();
        final ExecutionYear executionYear = executionDegree.getExecutionYear();
        final DegreeInfo degreeInfo = degree.getMostRecentDegreeInfo(executionYear.getAcademicInterval());
        final JsonObject data = serialize(executionDegree);
        addIfAndFormatElement(
                data,
                "accessRequisites",
                degreeInfo.getAccessRequisites(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "description",
                degreeInfo.getDescription(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "designedFor",
                degreeInfo.getDesignedFor(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "history",
                degreeInfo.getHistory(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "objectives",
                degreeInfo.getObjectives(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "operationalRegime",
                degreeInfo.getOperationalRegime(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "professionalExits",
                degreeInfo.getProfessionalExits(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "tuitionFees",
                degreeInfo.getGratuity(),
                LocalizedString::json
        );
        addIfAndFormatElement(
                data,
                "coordinators",
                executionDegree.getResponsibleCoordinators(),
                teachers -> teachers.stream()
                        .map(Coordinator::getTeacher)
                        .map(this.getAPISerializer().getTeacherSerializer()::serialize)
                        .collect(StreamUtils.toJsonArray())
        );
        return data;
    }

}
