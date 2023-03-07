package pt.ist.tecnicoapi.ui;

import org.apache.commons.io.IOUtils;
import org.fenixedu.academic.domain.space.SpaceUtils;
import org.fenixedu.spaces.domain.Space;
import org.fenixedu.spaces.services.SpaceBlueprintsDWGProcessor;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.ist.tecnicoapi.util.APIError;

import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/tecnico-api/v2")
public class SpaceController extends BaseController {

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/spaces", method = RequestMethod.GET)
    protected ResponseEntity<?> getAllSpaces(@RequestParam(required = false) Space campus) {
        if (campus != null) {
            if (!SpaceUtils.isCampus(campus)) {
                throw new APIError(HttpStatus.BAD_REQUEST, "error.space.campus.invalid", campus.getExternalId());
            }
            // FIXME: remove the isActive filter whenever getChildTree implements the check itself
            return respond(campus.getChildTree().stream().filter(Space::isActive).map(this::toBasicSpaceJson));
        }
        return respond(Space.getSpaces().map(this::toBasicSpaceJson));
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/spaces/buildings", method = RequestMethod.GET)
    protected ResponseEntity<?> getBuildings(@RequestParam(required = false) Space campus) {
        Stream<Space> buildings = SpaceUtils.buildings().stream();
        if (campus != null) {
            if (!SpaceUtils.isCampus(campus)) {
                throw new APIError(HttpStatus.BAD_REQUEST, "error.space.campus.invalid", campus.getExternalId());
            }
            buildings = buildings.filter(b -> SpaceUtils.getSpaceCampus(b).equals(campus));
        }
        return respond(buildings.map(this::toBasicSpaceJson));
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/spaces/campi", method = RequestMethod.GET)
    protected ResponseEntity<?> getCampi() {
        return respond(Space.getTopLevelSpaces().stream().map(this::toSpaceJson));
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/spaces/{space}", method = RequestMethod.GET)
    protected ResponseEntity<?> getSpace(@PathVariable Space space) {
        return ok(toSpaceJson(space));
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/spaces/{space}/day/{day}", method = RequestMethod.GET)
    protected ResponseEntity<?> getSpaceInDay(@PathVariable Space space, @PathVariable String day) {
        final DateTime start = parseDateTimeOrThrow(day);
        final Interval interval = parseIntervalOrThrow(start, start.plusDays(1));
        return ok(toExtendedSpaceJson(space, interval));
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/spaces/{space}/interval/{startDay}/{endDay}", method = RequestMethod.GET)
    protected ResponseEntity<?> getSpaceInInterval(@PathVariable Space space,
                                                   @PathVariable String startDay,
                                                   @PathVariable String endDay) {
        final DateTime start = parseDateTimeOrThrow(startDay);
        final DateTime end = parseDateTimeOrThrow(endDay);
        final Interval interval = parseIntervalOrThrow(start, end);
        return ok(toExtendedSpaceJson(space, interval));
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/spaces/{space}/blueprint/dwg", method = RequestMethod.GET)
    protected void getSpaceBlueprintDwg(HttpServletResponse response, @PathVariable Space space) {
        final InputStream inputStream = space.getBlueprintFile()
                .map(Optional::of)
                .orElseGet(
                        () -> Optional.ofNullable(
                                SpaceBlueprintsDWGProcessor
                                        .getSuroundingSpaceMostRecentBlueprint(space)
                        ).flatMap(Space::getBlueprintFile)
                )
                .orElseThrow(
                        () -> new APIError(
                                HttpStatus.NOT_FOUND,
                                "error.space.blueprint.unavailable",
                                space.getExternalId()
                        )
                )
                .getStream();

        try {
            final String filename = space.getExternalId() + ".dwg";
            response.setContentType("application/dwg");
            response.addHeader("Content-Disposition", "attachment; filename=" + filename);

            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (final IOException e) {
            throw new Error(e);
        }
    }

    @CrossOrigin(allowCredentials = "false")
    @RequestMapping(value = "/spaces/{space}/blueprint/jpeg", method = RequestMethod.GET)
    protected void getSpaceBlueprintJpeg(HttpServletResponse response, @PathVariable Space space) {
        try {
            final String filename = space.getExternalId() + ".jpg";
            response.setContentType("image/jpeg");
            response.addHeader("Content-Disposition", "attachment; filename=" + filename);

            SpaceBlueprintsDWGProcessor.writeBlueprint(
                    space,
                    new DateTime(),
                    false,
                    true,
                    true,
                    false,
                    new BigDecimal(100),
                    response.getOutputStream()
            );

            response.flushBuffer();
        } catch (UnavailableException e) {
            throw new APIError(
                    HttpStatus.NOT_FOUND,
                    "error.space.blueprint.unavailable",
                    space.getExternalId()
            );
        } catch (IOException e) {
            throw new Error(e);
        }
    }

}
