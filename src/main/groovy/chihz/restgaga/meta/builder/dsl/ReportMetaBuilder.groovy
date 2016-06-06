package chihz.restgaga.meta.builder.dsl

import chihz.restgaga.meta.ReportMeta

/**
 * 该Builder允许以下面这种形式DSL来构建ReportMeta对象:
 *
 * <pre>
 *     report stdout {*          action text
 *          args (
 *              output: System.out
 *          )
 *}* </pre>
 */
class ReportMetaBuilder extends BaseDSLBuilder {

    def String action

    def String[] env = new String[0]

    def Map args = [:]

    def action(String action) {
        this.action = action
        this
    }

    def args() {
        this.args = args
        this
    }

    def env(String... env) {
        this.env = env
        this
    }

    @Override
    def newInstance() {
        return new ReportMeta(name, action, this.env as String[], args)
    }
}
