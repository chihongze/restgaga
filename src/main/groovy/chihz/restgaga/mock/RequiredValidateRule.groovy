package chihz.restgaga.mock

/**
 * 该规则用于验证目标字符串是否为空
 */
class RequiredValidateRule extends AbstractValidateRule {

    /** 如果required为true 则目标字段是必须的 如果为false则目标字段为可选 */
    def final boolean required

    def RequiredValidateRule(String name, Map result, boolean required) {
        super(name, result)
        this.required = required
    }

    def boolean check(String text) {
        if (required) {
            if (text == null) {
                return false
            }
            return text.trim().asBoolean()
        }
        return true
    }
}
