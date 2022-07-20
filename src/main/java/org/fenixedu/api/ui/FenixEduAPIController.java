package org.fenixedu.api.ui;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.api.util.APIScope;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/fenixedu-api/v2")
public class FenixEduAPIController extends BaseController {

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public ResponseEntity<?> health() {
        return ok(new JsonObject());
    }

    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public ResponseEntity<?> about() {
        final Bennu bennu = Bennu.getInstance();
        final Unit institution = bennu.getInstitutionUnit();
        final ExecutionSemester currentSemester = ExecutionSemester.readActualExecutionSemester();

        return ok(data -> {
            JsonUtils.addIf(data, "institution", toUnitJson(institution));
            JsonUtils.addIf(data, "activeSemester", toExecutionSemesterJson(currentSemester));
            data.add("languages", JsonUtils.toJsonArray(languages -> CoreConfiguration.supportedLocales().stream()
                    .map(BaseController::toLocaleJson)
                    .forEach(languages::add))
            );
            data.add("defaultLanguage", toLocaleJson(Locale.getDefault()));

            // FIXME add RSS feed urls (?)
        });
    }

}
