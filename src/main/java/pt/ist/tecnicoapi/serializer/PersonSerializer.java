package pt.ist.tecnicoapi.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.fenixedu.academic.domain.Department;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.accessControl.ActiveStudentsGroup;
import org.fenixedu.academic.domain.accessControl.ActiveTeachersGroup;
import org.fenixedu.academic.domain.accessControl.AllAlumniGroup;
import org.fenixedu.academic.domain.contacts.EmailAddress;
import org.fenixedu.academic.domain.contacts.PartyContact;
import org.fenixedu.academic.domain.organizationalStructure.DepartmentUnit;
import org.fenixedu.academic.domain.organizationalStructure.ScientificAreaUnit;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.util.ContentType;
import pt.ist.fenixedu.contracts.domain.organizationalStructure.EmployeeContract;
import pt.ist.tecnicoapi.util.PhotoUtils;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class PersonSerializer extends DomainObjectSerializer {

    private final List<BiConsumer<JsonObject, Person>> roleProviders = new ArrayList<>();

    protected PersonSerializer(@NotNull TecnicoAPISerializer apiSerializer) {
        super(apiSerializer);

        roleProviders.add(this::provideStudentRole);
        roleProviders.add(this::provideAlumniRole);
        roleProviders.add(this::provideTeacherRole);
    }

    public @NotNull JsonObject serializeBasic(final @NotNull Person person) {
        return JsonUtils.toJson(data -> {
            final String primaryEmail = Optional.ofNullable(person.getDefaultEmailAddress())
                    .map(PartyContact::getPresentationValue)
                    .orElseGet(person::getInstitutionalEmailAddressValue);

            data.addProperty("username", person.getUsername());
            data.addProperty("displayName", person.getDisplayName());
            data.addProperty("email", primaryEmail);
        });
    }

    public @NotNull JsonObject serialize(final @NotNull Person person) {
        final JsonObject data = serializeBasic(person);

        data.addProperty("name", person.getName());
        data.addProperty("givenNames", person.getProfile().getGivenNames());
        data.addProperty("familyNames", person.getProfile().getFamilyNames());

        data.addProperty("gender", person.getGender().name());
        addIfAndFormat(
                data,
                "dateOfBirth",
                person.getDateOfBirthYearMonthDay(),
                dob -> dob.toLocalDate().toString()
        );
        addIfAndFormat(data, "institutionalEmail", person.getInstitutionalEmailAddress(), EmailAddress::getValue);

        if (person.getStudent() != null) {
            addIfAndFormat(
                    data,
                    "campus",
                    person.getStudent().getLastActiveRegistration(),
                    reg -> reg.getCampus().getName()
            );
        }

        addPersonContactJson(data, person, false);

        data.add("roles", serializePersonRoles(person));

        data.add("photo", serializePersonPhotoDataUri(person));

        return data;
    }

    public @NotNull JsonObject serializePersonPhotoDataUri(final @NotNull Person person) {
        String base64Png = PhotoUtils.toBase64Png(person, true);
        String mimeType = ContentType.PNG.getMimeType();
        return JsonUtils.toJson(photo -> {
            photo.addProperty("type", mimeType);
            photo.addProperty("data", base64Png);
        });
    }

    private void addPersonContactJson(final @NotNull JsonObject data,
                                      final @NotNull Person person,
                                      boolean checkAccess) {
        final JsonArray personalEmailAddresses = collectPartyContactStream(
                person.getEmailAddressStream().filter(PartyContact::isPersonalType),
                checkAccess
        );
        final JsonArray workEmailAddresses = collectPartyContactStream(
                person.getEmailAddressStream().filter(PartyContact::isWorkType),
                checkAccess
        );

        final JsonArray personalWebAddresses = collectPartyContactStream(
                person.getWebAddresses().stream().filter(PartyContact::isPersonalType),
                checkAccess
        );
        final JsonArray workWebAddresses = collectPartyContactStream(
                person.getWebAddresses().stream().filter(PartyContact::isWorkType),
                checkAccess
        );

        data.add("personalEmails", personalEmailAddresses);
        data.add("workEmails", workEmailAddresses);
        data.add("personalWebAddresses", personalWebAddresses);
        data.add("workWebAddresses", workWebAddresses);
    }

    private @NotNull JsonArray collectPartyContactStream(@NotNull Stream<? extends PartyContact> contactStream,
                                                         boolean checkAccess) {
        return contactStream.filter(PartyContact::isActiveAndValid)
                .filter(contact -> !checkAccess || contact.isVisible())
                .map(PartyContact::getPresentationValue)
                .map(JsonPrimitive::new)
                .collect(StreamUtils.toJsonArray());
    }

    public @NotNull JsonObject serializePersonRoles(final @NotNull Person person) {
        return JsonUtils.toJson(
                roles -> this.roleProviders.forEach(roleProvider -> roleProvider.accept(roles, person))
        );
    }

    /**
     * Adds the (serialized) student role, if applicable, to the roles JSON object.
     *
     * @param roles  The roles JSON object to append data to.
     * @param person The person to check for and serialize the student role.
     */
    private void provideStudentRole(@NotNull JsonObject roles, @NotNull Person person) {
        final User user = person.getUser();

        if (!new ActiveStudentsGroup().isMember(user)) {
            return;
        }

        roles.add("student", JsonUtils.toJson(role -> {
            final JsonArray registrations = person.getStudent()
                    .getActiveRegistrationStream()
                    .map(getAPISerializer().getRegistrationSerializer()::serialize)
                    .collect(StreamUtils.toJsonArray());
            role.add("registrations", registrations);
        }));
    }

    /**
     * Adds the (serialized) alumni role, if applicable, to the roles JSON object.
     *
     * @param roles  The roles JSON object to append data to.
     * @param person The person to check for and serialize the alumni role.
     */
    private void provideAlumniRole(@NotNull JsonObject roles, @NotNull Person person) {
        final User user = person.getUser();

        // FIXME The new `AlumniGroup.get().isMember(user)` method does not produce the same result
        if (!new AllAlumniGroup().isMember(user)) {
            return;
        }

        roles.add("alumni", JsonUtils.toJson(role -> {
            final JsonArray registrations = person.getStudent()
                    .getConcludedRegistrations()
                    .stream()
                    .map(getAPISerializer().getRegistrationSerializer()::serialize)
                    .collect(StreamUtils.toJsonArray());
            role.add("concludedRegistrations", registrations);
        }));
    }

    /**
     * Adds the (serialized) teacher role, if applicable, to the roles JSON object.
     *
     * @param roles  The roles JSON object to append data to.
     * @param person The person to check for and serialize the teacher role.
     */
    private void provideTeacherRole(@NotNull JsonObject roles, @NotNull Person person) {
        final User user = person.getUser();

        if (!new ActiveTeachersGroup().isMember(user)) {
            return;
        }

        final Teacher teacher = person.getTeacher();
        final Department department = teacher.getDepartment();
        final DepartmentUnit departmentUnit = department.getDepartmentUnit();
        roles.add("teacher", JsonUtils.toJson(role -> {
            role.add("department", getAPISerializer().getDepartmentSerializer().serialize(department));
            addIfAndFormatElement(
                    role,
                    "category",
                    teacher.getCategory(),
                    teacherCategory -> teacherCategory.getName().json()
            );
            role.add(
                    "scientificAreas",
                    departmentUnit.getAllSubUnits()
                            .stream()
                            .filter(Unit::isScientificAreaUnit)
                            .map(ScientificAreaUnit.class::cast)
                            .filter(scientificAreaUnit -> isCurrentlyMemberOfScientificArea(scientificAreaUnit, person))
                            .map(getAPISerializer().getScientificAreaSerializer()::serialize)
                            .collect(StreamUtils.toJsonArray())
            );
        }));
    }

    private boolean isCurrentlyMemberOfScientificArea(ScientificAreaUnit scientificAreaUnit, Person person) {
        return EmployeeContract.getWorkingContracts(scientificAreaUnit)
                .stream()
                .anyMatch(contract -> contract.getPerson().equals(person));
    }
}
