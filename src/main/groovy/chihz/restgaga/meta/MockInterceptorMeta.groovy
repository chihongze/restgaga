package chihz.restgaga.meta


class MockInterceptorMeta extends BaseMeta {

    def final Closure interceptAction

    def MockInterceptorMeta(String name, Closure interceptAction) {
        super(name)
        this.interceptAction = interceptAction
    }
}
