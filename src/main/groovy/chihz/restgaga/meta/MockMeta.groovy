package chihz.restgaga.meta

public class MockMeta extends BaseMeta {

    def final Closure mockAction

    def MockMeta(String name, Closure mockAction) {
        super(name)
        this.mockAction = mockAction
    }
}
