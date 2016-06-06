package chihz.restgaga.meta


class MetaListFactoryTestCase extends GroovyTestCase {

    def MetaListFactory mlf

    def Expando mockCtx

    void setUp() {
        mlf = new MetaListFactory()
        mockCtx = new Expando()
        mockCtx.x = 1
        mockCtx.y = 2
    }

    void testStaticList() {
        mlf ^ [1, 2, 3]
        assertEquals(mlf.operation, MetaListFactory.REPLACE)
        assertEquals(mlf.list, [1, 2, 3])

        mlf << [1, 2, 3]
        assertEquals(mlf.operation, MetaListFactory.APPEND)
        assertEquals(mlf.list, [1, 2, 3])
    }

    void testClosure() {
        mlf ^ { [x, y, x + 1, y + 1] }
        assertEquals(mlf.operation, MetaListFactory.REPLACE)
        assertEquals(mlf.getList(mockCtx), [1, 2, 2, 3])

        mlf << { [x + 1, y + 1] }
        assertEquals(mlf.operation, MetaListFactory.APPEND)
        assertEquals(mlf.getList(mockCtx), [2, 3])
    }
}
