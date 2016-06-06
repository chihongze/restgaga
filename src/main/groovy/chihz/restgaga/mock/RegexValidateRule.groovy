package chihz.restgaga.mock

import java.util.regex.Pattern


public class RegexValidateRule extends AbstractValidateRule {

    def final Pattern pattern

    def RegexValidateRule(String name, Map result, Object pattern) {
        super(name, result)
        if (pattern instanceof String) {
            this.pattern = Pattern.compile(pattern)
        } else if (pattern instanceof Pattern) {
            this.pattern = pattern
        } else {
            throw new IllegalArgumentException(
                    "Invalid pattern type: ${pattern.class.name}")
        }
    }

    def boolean check(String text) {
        if (!text) {
            return true
        }
        return text ==~ pattern
    }
}
