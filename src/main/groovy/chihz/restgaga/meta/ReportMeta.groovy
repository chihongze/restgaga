package chihz.restgaga.meta


class ReportMeta extends BaseMeta {

    def final String action

    def final String[] env

    def final Map args

    def ReportMeta(String name, String action, String[] env, Map args) {
        super(name)
        this.action = action
        this.env = env
        this.args = args
    }
}
