package chihz.restgaga.runner

import chihz.restgaga.meta.MetaMapFactory
import org.apache.commons.collections.map.CaseInsensitiveMap


class TestCaseContextTestCase extends GroovyTestCase {

    void testUpdateHeaders() {
        TestCaseContext ctx = new TestCaseContext("test")

        def map1 = new MetaMapFactory()
        map1(a: 1, b: 2)

        def map2 = new MetaMapFactory()
        map2 {
            [A: 3, c: 5, d: "$currentEnv"]
        }

        ctx.updateHeaders(map1, map2)
        assertEquals(ctx.headers, new CaseInsensitiveMap([a: 3, c: 5, b: 2, d: "test"]))

        ctx = new TestCaseContext("pro")

        def map3 = new MetaMapFactory()
        map3 {
            [c: 10]
        }

        ctx.updateHeaders(map1, map2, map3)
        assertEquals(ctx.headers, new CaseInsensitiveMap([A: 3, c: 10, b: 2, d: "pro"]))
    }
}
