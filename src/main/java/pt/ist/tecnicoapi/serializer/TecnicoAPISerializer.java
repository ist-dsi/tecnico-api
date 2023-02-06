package pt.ist.tecnicoapi.serializer;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class TecnicoAPISerializer {

    private final @NotNull AttendsSerializer attendsSerializer = new AttendsSerializer(this);
    private final @NotNull BibliographicReferenceSerializer bibliographicReferenceSerializer = new BibliographicReferenceSerializer(
            this
    );
    private final @NotNull CourseGroupSerializer courseGroupSerializer = new CourseGroupSerializer(this);
    private final @NotNull CourseLoadSerializer courseLoadSerializer = new CourseLoadSerializer(this);
    private final @NotNull CurricularCourseSerializer curricularCourseSerializer = new CurricularCourseSerializer(
            this
    );
    private final @NotNull CurricularPeriodSerializer curricularPeriodSerializer = new CurricularPeriodSerializer(
            this
    );
    private final @NotNull CurriculumLineSerializer curriculumLineSerializer = new CurriculumLineSerializer(
            this
    );
    private final @NotNull DegreeSerializer degreeSerializer = new DegreeSerializer(this);
    private final @NotNull EnrolmentSerializer enrolmentSerializer = new EnrolmentSerializer(this);
    private final @NotNull EnrolmentPolicySerializer enrolmentPolicySerializer = new EnrolmentPolicySerializer(
            this
    );
    private final @NotNull EvaluationTypeSerializer evaluationTypeSerializer = new EvaluationTypeSerializer(this);
    private final @NotNull EvaluationSeasonSerializer evaluationSeasonSerializer = new EvaluationSeasonSerializer(this);
    private final @NotNull EvaluationSerializer evaluationSerializer = new EvaluationSerializer(this);
    private final @NotNull EventSerializer eventSerializer = new EventSerializer(this);
    private final @NotNull EventBeanSerializer eventBeanSerializer = new EventBeanSerializer(this);
    private final @NotNull ExecutionDegreeSerializer executionDegreeSerializer = new ExecutionDegreeSerializer(
            this
    );
    private final @NotNull ExecutionCourseSerializer executionCourseSerializer = new ExecutionCourseSerializer(
            this
    );
    private final @NotNull ExecutionSemesterSerializer executionSemesterSerializer = new ExecutionSemesterSerializer(
            this
    );
    private final @NotNull ExecutionYearSerializer executionYearSerializer = new ExecutionYearSerializer(this);
    private final @NotNull GroupingSerializer groupingSerializer = new GroupingSerializer(this);
    private final @NotNull LessonSerializer lessonSerializer = new LessonSerializer(this);
    private final @NotNull LessonInstanceSerializer lessonInstanceSerializer = new LessonInstanceSerializer(this);
    private final @NotNull LocaleSerializer localeSerializer = new LocaleSerializer(this);
    private final @NotNull OccupationSerializer occupationSerializer = new OccupationSerializer(this);
    private final @NotNull PersonSerializer personSerializer = new PersonSerializer(this);
    private final @NotNull ProfessorshipSerializer professorshipSerializer = new ProfessorshipSerializer(this);
    private final @NotNull RegistrationSerializer registrationSerializer = new RegistrationSerializer(this);
    private final @NotNull ScheduleSerializer scheduleSerializer = new ScheduleSerializer(this);
    private final @NotNull SchoolClassSerializer schoolClassSerializer = new SchoolClassSerializer(this);
    private final @NotNull ShiftSerializer shiftSerializer = new ShiftSerializer(this);
    private final @NotNull SpaceSerializer spaceSerializer = new SpaceSerializer(this);
    private final @NotNull StudentGroupSerializer studentGroupSerializer = new StudentGroupSerializer(this);
    private final @NotNull TeacherSerializer teacherSerializer = new TeacherSerializer(this);
    private final @NotNull UnitSerializer unitSerializer = new UnitSerializer(this);

}
