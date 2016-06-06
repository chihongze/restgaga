package chihz.restgaga.mock


abstract class AbstractValidateRule {

    def final String name

    def final Map result

    def AbstractValidateRule(String name, Map result) {
        this.name = name
        this.result = result
    }

    def abstract boolean check(String text)
}
