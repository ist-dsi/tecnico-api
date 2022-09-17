package org.fenixedu.api.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.accessControl.ActiveStudentsGroup;
import org.fenixedu.academic.domain.accessControl.ActiveTeachersGroup;
import org.fenixedu.academic.domain.accessControl.AllAlumniGroup;
import org.fenixedu.academic.domain.contacts.EmailAddress;
import org.fenixedu.academic.domain.contacts.PartyContact;
import org.fenixedu.academic.util.ContentType;
import org.fenixedu.api.util.APIScope;
import org.fenixedu.api.util.PhotoUtils;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.commons.stream.StreamUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/fenixedu-api/v2")
public class PersonalController extends BaseController {

    @RequestMapping(value = "/person", method = RequestMethod.GET)
    public ResponseEntity<?> getPerson(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken) {
        requireOAuthScope(accessToken, APIScope.PERSONAL_READ);

        final Person person = Authenticate.getUser().getPerson();

        return ok(toPersonJson(person));
    }

    protected @NotNull JsonObject toPersonJson(final @NotNull Person person) {
        return JsonUtils.toJson(data -> {
            data.addProperty("username", person.getUsername());

            data.addProperty("name", person.getName());
            data.addProperty("givenNames", person.getProfile().getGivenNames());
            data.addProperty("familyNames", person.getProfile().getFamilyNames());
            data.addProperty("displayName", person.getDisplayName());

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

            data.add("roles", toPersonRolesJson(person));

            data.add("photo", toPersonPhotoDataUri(person));
        });
    }

    protected @NotNull JsonObject toPersonPhotoDataUri(final @NotNull Person person) {
        String base64Png = PhotoUtils.toBase64Png(person, true);
        String mimeType = ContentType.PNG.getMimeType();
        return JsonUtils.toJson(photo -> {
            photo.addProperty("type", mimeType);
            photo.addProperty("data", base64Png);
        });
    }

    protected void addPersonContactJson(final @NotNull JsonObject data,
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

        final String primaryEmail = Optional.ofNullable(person.getDefaultEmailAddress())
                .map(PartyContact::getPresentationValue)
                .orElseGet(person::getInstitutionalEmailAddressValue);

        data.add("personalEmails", personalEmailAddresses);
        data.add("workEmails", workEmailAddresses);
        data.add("personalWebAddresses", personalWebAddresses);
        data.add("workWebAddresses", workWebAddresses);
        data.addProperty("email", primaryEmail);
    }

    private @NotNull JsonArray collectPartyContactStream(@NotNull Stream<? extends PartyContact> contactStream,
                                                         boolean checkAccess) {
        return contactStream.filter(PartyContact::isActiveAndValid)
                .filter(contact -> !checkAccess || contact.isVisible())
                .map(PartyContact::getPresentationValue)
                .map(JsonPrimitive::new)
                .collect(StreamUtils.toJsonArray());
    }

    protected @NotNull JsonObject toPersonRolesJson(final @NotNull Person person) {
        final User user = person.getUser();
        final JsonObject roles = new JsonObject();

        if (new ActiveStudentsGroup().isMember(user)) {
            roles.add("student", JsonUtils.toJson(role -> {
                final JsonArray registrations = person.getStudent()
                        .getActiveRegistrationStream()
                        .map(this::toRegistrationJson)
                        .collect(StreamUtils.toJsonArray());
                role.add("registrations", registrations);
            }));
        }

        // FIXME The new `AlumniGroup.get().isMember(user)` method does not produce the same result
        if (new AllAlumniGroup().isMember(user)) {
            roles.add("alumni", JsonUtils.toJson(role -> {
                final JsonArray registrations = person.getStudent()
                        .getConcludedRegistrations()
                        .stream()
                        .map(this::toRegistrationJson)
                        .collect(StreamUtils.toJsonArray());
                role.add("concludedRegistrations", registrations);
            }));
        }

        if (new ActiveTeachersGroup().isMember(user)) {
            roles.add("teacher", JsonUtils.toJson(role -> {
                final JsonObject department = toUnitJson(person.getTeacher().getDepartment().getDepartmentUnit());
                role.add("department", department);
            }));
        }

        // FIXME get employees, researchers and grant owners (only available through fenixedu-ist-integration)

        return roles;
    }

}
