package org.fenixedu.api.ui;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Optional;

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
            JsonUtils.addIf(data, "activeSemester", toExtendedExecutionSemesterJson(currentSemester));
            data.add(
                    "languages",
                    JsonUtils.toJsonArray(
                            languages -> CoreConfiguration.supportedLocales()
                                    .stream()
                                    .map(this::toLocaleJson)
                                    .forEach(languages::add)
                    )
            );
            data.add("defaultLanguage", toLocaleJson(Locale.getDefault()));

            // FIXME add RSS feed urls (?)
        });
    }

    // gets all terms
    @RequestMapping(value = "/academicterms", method = RequestMethod.GET)
    public ResponseEntity<?> getAcademicTerms(@RequestParam(required = false) Optional<String> from) {
        final Bennu bennu = Bennu.getInstance();
        final ExecutionYear firstExecutionYear = from
                .map(this::parseExecutionYearOrThrow)
                .orElse(ExecutionYear.readFirstExecutionYear());

        return respond(
                bennu.getExecutionYearsSet()
                        .stream()
                        .filter(executionYear -> executionYear.isAfterOrEquals(firstExecutionYear))
                        .sorted(ExecutionYear.REVERSE_COMPARATOR_BY_YEAR)
                        .map(this::toExtendedExecutionYearJson)
        );
    }

    // gets a specific term
    @RequestMapping(value = "/academicterms/{beginYear}/{endYear}", method = RequestMethod.GET)
    public ResponseEntity<?> getAcademicTerm(@PathVariable String beginYear, @PathVariable String endYear) {
        final ExecutionYear executionYear = parseExecutionYearOrThrow(beginYear + "/" + endYear);
        return ok(toExtendedExecutionYearJson(executionYear));
    }

}
