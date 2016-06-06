package chihz.restgaga.meta.builder.dsl
// 名称 -> 动作 名值对封装
class NamedAction {

    def final String name

    def final action

    def NamedAction(String name, action) {
        this.name = name
        this.action = action
    }

}

/**
 * <p>
 *     基于Groovy闭包的DSL元素构建者
 * </p>
 */
abstract class BaseDSLBuilder {

    def String name

    def name(String name) {
        this.name = name
        this
    }

    def build(buildAction = null) {
        if (buildAction == null) {
            return newInstance()
        }
        if (buildAction instanceof NamedAction) {
            this.name = buildAction.name
            buildAction = buildAction.action
        }
        buildAction.delegate = this
        buildAction()
        newInstance()
    }

    def abstract newInstance()
}
