package org.fenixedu.api.ui;

import com.google.gson.JsonObject;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.organizationalStructure.Unit;
import org.fenixedu.api.util.ApiError;
import org.fenixedu.bennu.core.json.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;

public class BaseController extends org.fenixedu.bennu.spring.BaseController {

    @ExceptionHandler({ApiError.class})
    public ResponseEntity<?> handleApiError(final ApiError error) {
        return ResponseEntity.status(error.getStatus()).body(error.toResponseBody());
    }

    static JsonObject toUnitJson(Unit unit) {
        if (unit == null) {
            return null;
        }
        return JsonUtils.toJson(institution -> {
            institution.addProperty("name", unit.getName());
            if (unit.hasDefaultWebAddress()) {
                institution.addProperty("url", unit.getDefaultWebAddressUrl());
            }
            if (unit.hasDefaultEmailAddress()) {
                institution.addProperty("email", unit.getDefaultEmailAddressValue());
            }
            if (unit.hasDefaultMobilePhone()) {
                institution.addProperty("phone", unit.getDefaultMobilePhoneNumber());
            }
        });
    }

    static JsonObject toExecutionSemesterJson(ExecutionSemester executionSemester) {
        if (executionSemester == null) {
            return null;
        }
        return JsonUtils.toJson(semester -> {
            semester.addProperty("displayName", executionSemester.getQualifiedName());
            semester.addProperty("semester", executionSemester.getSemester());
            semester.addProperty("beginDate", executionSemester.getBeginLocalDate().toString());
            semester.addProperty("endDate", executionSemester.getEndLocalDate().toString());
            JsonUtils.addIf(semester, "year", toExecutionYearJson(executionSemester.getExecutionYear()));
        });
    }

    static JsonObject toExecutionYearJson(ExecutionYear executionYear) {
        if (executionYear == null) {
            return null;
        }
        return JsonUtils.toJson(year -> {
            year.addProperty("displayName", executionYear.getQualifiedName());
            year.addProperty("beginYear", executionYear.getBeginCivilYear());
            year.addProperty("endYear", executionYear.getEndCivilYear());
            year.addProperty("beginDate", executionYear.getBeginLocalDate().toString());
            year.addProperty("endDate", executionYear.getEndLocalDate().toString());
        });
    }

    static JsonObject toLocaleJson(Locale locale) {
        return JsonUtils.toJson(localeJson -> {
            localeJson.addProperty("locale", locale.toLanguageTag());
            localeJson.addProperty("name", locale.getDisplayName());
        });
    }

}
