package org.fenixedu.api.ui;

import org.fenixedu.academic.domain.Person;
import org.fenixedu.api.util.APIScope;
import org.fenixedu.bennu.core.security.Authenticate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fenixedu-api/v2")
public class PersonalController extends BaseController {

    @RequestMapping(value = "/person", method = RequestMethod.GET)
    public ResponseEntity<?> getPerson(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken) {
        requireOAuthScope(accessToken, APIScope.PERSONAL_READ);

        final Person person = Authenticate.getUser().getPerson();

        return ok(toPersonJson(person));
    }

}
