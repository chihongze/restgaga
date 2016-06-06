package chihz.restgaga.mock

import chihz.restgaga.util.Lang
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test


class ValidateContextTestCase {

    def validateClosure = {
        nameRequired true, [code: 400, msg: "name empty"]
        nameMinLength 6, [code: 400, msg: "name too short"]
        nameMaxLength 20, [code: 400, msg: "name too long"]
        passwordRequired true, [code: 400, msg: "password empty"]
        passwordMinLength 6, [code: 400, msg: "password too short"]
    }

    def validateClosure2 = {
        nameMinLength 6, [code: 400, msg: "name too short"]
        nameMaxLength 20, [code: 400, msg: "name too long"]
        nameRegex '^[a-zA-Z0-9]*$', [code: 400, msg: "name format error"]
        nameLogic({ "samchi" != it }, [code: 400, msg: "name existed"])
    }


    @Test
    void testValidate() {
        def ctx = new ValidateContext()
        Lang.cloneAndRun(validateClosure, ctx)
        JSONObject jsonBody = new JSONObject([name: "samchi", password: "123456"])
        def extractor = { String fieldName ->
            jsonBody.has(fieldName) ? jsonBody.get(fieldName).toString() : ""
        }
        def result = ctx.validate extractor
        Assert.assertNull(result)

        jsonBody.put("name", "")
        result = ctx.validate extractor
        Assert.assertEquals(result, [code: 400, msg: "name empty"])

        jsonBody.put("name", "samchi")
        jsonBody.put("password", "aaa")
        result = ctx.validate extractor
        Assert.assertEquals(result, [code: 400, msg: "password too short"])
    }

    @Test
    void testValidateField() {
        def ctx = new ValidateContext()
        Lang.cloneAndRun(validateClosure, ctx)
        def rs = ctx.validateField("name", "aaa")
        Assert.assertEquals(rs, [code: 400, msg: "name too short"])
        rs = ctx.validateField("name", "aaa" * 10)
        Assert.assertEquals(rs, [code: 400, msg: "name too long"])
        rs = ctx.validateField("name", "")
        Assert.assertEquals(rs, [code: 400, msg: "name empty"])
        rs = ctx.validateField("name", "a" * 10)
        Assert.assertNull(rs)

        ctx = new ValidateContext()
        Lang.cloneAndRun(validateClosure2, ctx)
        rs = ctx.validateField("name", "")
        Assert.assertNull(rs)
        rs = ctx.validateField("name", "samchi")
        Assert.assertEquals(rs, [code: 400, msg: "name existed"])
        rs = ctx.validateField("name", "*&*&SamChi")
        Assert.assertEquals(rs, [code: 400, msg: "name format error"])
    }
}
