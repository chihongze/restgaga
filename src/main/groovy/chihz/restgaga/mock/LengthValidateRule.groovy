package chihz.restgaga.mock

/**
 * 用于验证目标字段长度
 */
class LengthValidateRule extends AbstractValidateRule {

    def final String compareAction

    def final int extremeValue

    def LengthValidateRule(String name, Map result,
                           String compareAction, int extremeValue) {
        super(name, result)
        this.compareAction = compareAction
        this.extremeValue = extremeValue
    }

    def boolean check(String text) {
        if (!text) {
            return true
        }

        int targetLength = text.length()

        if (compareAction == "max") {
            return targetLength <= this.extremeValue
        } else if (compareAction == "min") {
            return targetLength >= this.extremeValue
        } else {
            throw new IllegalArgumentException(
                    "Invalid compare action, only support 'min' or 'max'")
        }
    }
}
