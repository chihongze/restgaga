package chihz.restgaga.mock

import org.junit.Assert
import org.junit.Test

class ValidateRuleTestCase {

    @Test
    void testRequiredValidateRule() {
        RequiredValidateRule rule = new RequiredValidateRule(
                "username",
                [code: 400, msg: "username empty"], true)
        Assert.assertFalse(rule.check(""))  // 检查不通过
        Assert.assertFalse(rule.check(null))
        Assert.assertTrue(rule.check("samchi"))
    }

    @Test
    void testLengthValidateRule() {
        LengthValidateRule rule = new LengthValidateRule("username",
                [code: 400, msg: "username too short"], "min", 6)
        Assert.assertFalse(rule.check("aaa"))
        Assert.assertTrue(rule.check("aaa" * 2))
        Assert.assertTrue(rule.check(""))

        rule = new LengthValidateRule("username",
                [code: 400, msg: "username too long"], "max", 20)
        Assert.assertTrue(rule.check("a" * 5))
        Assert.assertTrue(rule.check("a" * 20))
        Assert.assertFalse(rule.check("a" * 21))
    }

    @Test
    void testRegexValidateRule() {
        RegexValidateRule rule = new RegexValidateRule("num",
                [code: 400, msg: "num must be int"], '^\\d+$')
        Assert.assertFalse(rule.check("111a"))
        Assert.assertTrue(rule.check("1234"))
        Assert.assertTrue(rule.check(""))
    }

    @Test
    void testLogicValidateRule() {
        LogicValidateRule rule = new LogicValidateRule("email",
                [code: 400, msg: "email existed"], { !it.equals("chihongze@gmail.com") })
        Assert.assertTrue(rule.check("sam@gmail.com"))
        Assert.assertTrue(rule.check(""))
        Assert.assertFalse(rule.check("chihongze@gmail.com"))
    }
}
