package chihz.restgaga.mock


class LogicValidateRule extends AbstractValidateRule {

    def final Closure logic

    def LogicValidateRule(String name, Map result, Closure logic) {
        super(name, result)
        this.logic = logic
    }

    def boolean check(String text) {
        if (!text) {
            return true
        }

        return logic(text)
    }
}
