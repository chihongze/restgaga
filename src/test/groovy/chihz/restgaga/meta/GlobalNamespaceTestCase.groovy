package chihz.restgaga.meta


class GlobalNamespaceTestCase extends GroovyTestCase {

    def global = GlobalNamespace.instance

    void testInit() {
        global.x = 1
        global.y = 2
        global {
            delegate.z = "$it, ${x + y}"
        }
        global.init("test")
        assertEquals(global.z, "test, 3")
    }
}
