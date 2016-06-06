package chihz.restgaga.mock

import chihz.restgaga.meta.MockInterceptorMeta
import chihz.restgaga.meta.MockMeta
import chihz.restgaga.util.Lang
import org.json.JSONObject
import spark.*

/**
 * delegate of mockAction
 */
class MockController implements Route {

    def final MockMeta mockMeta

    def MockController(MockMeta mockMeta) {
        this.mockMeta = mockMeta
    }

    def handle(Request request, Response response) throws Exception {
        try {
            return Lang.cloneAndRun(mockMeta.mockAction, new ControllerRequestContext(request, response),
                    true)
        } catch (InvalidRequestException e) {
            return e.result
        }
    }

}

class MockInterceptor implements Filter {

    def final MockInterceptorMeta mockInterceptorMeta

    def MockInterceptor(MockInterceptorMeta mockInterceptorMeta) {
        this.mockInterceptorMeta = mockInterceptorMeta
    }

    @Override
    void handle(Request request, Response response) throws Exception {
        Lang.cloneAndRun(mockInterceptorMeta.interceptAction, new RequestContext(request, response))
    }
}


class RequestContext {

    def final Request req

    def final Response res

    def RequestContext(Request req, Response res) {
        this.req = req
        this.res = res
    }

    void halt() {
        throw new HaltException();
    }

    void halt(int status) {
        throw new HaltException(status);
    }

    void halt(String body) {
        throw new HaltException(body);
    }

    void halt(Map body) { throw new HaltException(new JSONObject(body).toString()) }

    void halt(int status, String body) {
        throw new HaltException(status, body);
    }

    void halt(int status, Map body) { throw new HaltException(status, new JSONObject(body).toString()) }
}

class ControllerRequestContext extends RequestContext {

    def final ValidateContext validateContext

    def ControllerRequestContext(Request request, Response response) {
        super(request, response)
        this.validateContext = new ValidateContext()
    }

    def validate(Closure validateLogic) {
        Lang.cloneAndRun(validateLogic, this.validateContext)
        String contentType = req.headers("Content-Type")
        // json body
        Closure extractor
        if (contentType && contentType.toLowerCase().contains("application/json")) {
            JSONObject jsonBody = req.jsonBody()
            extractor = { String fieldName ->
                jsonBody.has(fieldName) ? jsonBody.get(fieldName)?.toString() : ""
            }
        } else {
            extractor = { String fieldName ->
                req.queryParams(fieldName)
            }
        }

        def validateResult = this.validateContext.validate extractor
        if (validateResult != null) {
            throw new InvalidRequestException(validateResult)
        }
    }
}

class ValidateContext {

    def static final Map validators = [
            "Required" : { String name, Map result, boolean required ->
                new RequiredValidateRule(name, result, required)
            },
            "MinLength": { String name, Map result, int extremeValue ->
                new LengthValidateRule(name, result, "min", extremeValue)
            },
            "MaxLength": { String name, Map result, int extremeValue ->
                new LengthValidateRule(name, result, "max", extremeValue)
            },
            "Regex"    : { String name, Map result, Object pattern ->
                new RegexValidateRule(name, result, pattern)
            },
            "Logic"    : { String name, Map result, Closure logic ->
                new LogicValidateRule(name, result, logic)
            }
    ]

    def final Map<String, Map> rules = [:]


    def validate(Closure fieldExtractor) {
        for (String fieldName in rules.keySet()) {
            String fieldValue = fieldExtractor(fieldName)
            def validateResult = validateField(fieldName, fieldValue)
            if (validateResult != null) {
                return validateResult
            }
        }
    }

    def validateField(String fieldName, String fieldValue) {
        Map fieldRules = rules.get(fieldName)
        if (fieldRules == null) {
            return null
        }
        for (String ruleName in validators.keySet()) {
            AbstractValidateRule validator = fieldRules.get(ruleName)
            if (validator == null) {
                continue
            }
            boolean checkResult = validator.check(fieldValue)
            if (!checkResult) {
                return validator.result
            }
        }
        return null
    }

    def methodMissing(String name, args) {
        validators.each { String k, Closure v ->

            if (!name.endsWith(k)) {
                return
            }

            def fieldName = name[0..-(k.length() + 1)]
            Map fieldRules = rules.get(fieldName)
            if (fieldRules == null) {
                fieldRules = new HashMap()
                rules.put(fieldName, fieldRules)
            }
            fieldRules.put(k, v(fieldName, args[1], args[0]))
        }
    }
}

class InvalidRequestException extends Exception {

    def final Map result

    def InvalidRequestException(Map result) {
        super()
        this.result = result
    }
}
