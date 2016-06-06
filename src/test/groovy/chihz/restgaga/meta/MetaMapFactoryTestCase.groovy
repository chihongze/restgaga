package chihz.restgaga.meta

/**
 * Testcase of chihz.restgaga.meta.MetaMapFactory
 */

class MetaMapFactoryTestCase extends GroovyTestCase {

    def MetaMapFactory mmf = null

    def Expando mockCtx = null

    void setUp() {
        mmf = new MetaMapFactory()
        mockCtx = new Expando()
        mockCtx.x = 1
        mockCtx.y = 2
    }

    void testStaticMap() {
        mmf(
                name: "samchi",
                birth: "1989-11-07"
        )
        assertEquals(mmf.type, MetaMapFactory.STATIC_MAP)
        assertEquals(mmf.map, [name: "samchi", "birth": "1989-11-07"])
    }

    void testDynamicMap() {
        mmf(
                name: "samchi",
                grade: {
                    x
                }
        )
        assertEquals(mmf.type, MetaMapFactory.DYNAMIC_MAP)
        assertEquals(mmf.getMap(mockCtx), [name: "samchi", "grade": mockCtx.x])
    }

    void testClosure() {
        mmf {
            ["x": x, "y": y]
        }
        assertEquals(mmf.type, MetaMapFactory.CLOSURE)
        assertEquals(mmf.getMap(mockCtx), [x: mockCtx.x, y: mockCtx.y])
    }
}
