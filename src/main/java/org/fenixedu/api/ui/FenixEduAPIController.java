package org.fenixedu.api.ui;

import com.google.gson.JsonObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fenixedu-api")
public class FenixEduAPIController extends org.fenixedu.bennu.spring.BaseController {

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public ResponseEntity<?> health() {
        return ok(new JsonObject());
    }

}
