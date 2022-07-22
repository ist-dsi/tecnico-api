package org.fenixedu.api.ui;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.api.util.APIError;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.springframework.http.HttpStatus;
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
            JsonUtils.addIf(data, "activeSemester", toExecutionSemesterJson(currentSemester, true));
            data.add("languages", JsonUtils.toJsonArray(languages -> CoreConfiguration.supportedLocales().stream()
                    .map(this::toLocaleJson)
                    .forEach(languages::add))
            );
            data.add("defaultLanguage", toLocaleJson(Locale.getDefault()));

            // FIXME add RSS feed urls (?)
        });
    }

    // gets all terms
    @RequestMapping(value = "/academicterms", method = RequestMethod.GET)
    public ResponseEntity<?> academicTerms(@RequestParam(required = false) Optional<String> from) {
        final Bennu bennu = Bennu.getInstance();

        if (from.isPresent()) {
            final ExecutionYear firstExecutionYear = Optional.ofNullable(ExecutionYear.readExecutionYearByName(from.get()))
                    .orElseThrow(() -> new APIError(HttpStatus.BAD_REQUEST, "error.academicterm.year.incorrect", from.get()));

            return ok(bennu.getExecutionYearsSet().stream()
                    .filter(executionYear -> executionYear.isAfterOrEquals(firstExecutionYear))
                    .sorted(ExecutionYear.REVERSE_COMPARATOR_BY_YEAR)
                    .map(executionYear -> toExecutionYearJson(executionYear, true))
                    .collect(jsonArrayCollector)
            );
        }
        return ok(bennu.getExecutionYearsSet().stream()
                .sorted(ExecutionYear.REVERSE_COMPARATOR_BY_YEAR)
                .map(executionYear -> toExecutionYearJson(executionYear, true))
                .collect(jsonArrayCollector)
        );
    }

    // gets a specific term
    @RequestMapping(value = "/academicterms/{beginYear}/{endYear}", method = RequestMethod.GET)
    public ResponseEntity<?> getAcademicTerm(@PathVariable String beginYear, @PathVariable String endYear) {
        String year = beginYear + "/" + endYear;
        final ExecutionYear executionYear = Optional.ofNullable(ExecutionYear.readExecutionYearByName(year))
                .orElseThrow(() -> new APIError(HttpStatus.BAD_REQUEST, "error.academicterm.year.incorrect", year));
        return ok(toExecutionYearJson(executionYear, true));
    }

}
