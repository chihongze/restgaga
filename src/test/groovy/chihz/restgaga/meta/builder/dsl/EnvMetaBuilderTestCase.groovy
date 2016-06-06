package chihz.restgaga.meta.builder.dsl

import chihz.restgaga.meta.MetaListFactory


class EnvMetaBuilderTestCase extends DSLBuilderTestCase {

    void testBuild() {
        def builder = new EnvMetaBuilder()
        def env = builder.build test {
            baseUrl "http://test.hehe.com"
            headers(
                    Accept: "application/json",
                    "Accept-language": "en-US"
            )
            fixtures ^ ["sign", "token"]
        }
        assertEquals(env.name, "test")
        assertEquals(env.baseUrl, "http://test.hehe.com")
        assertEquals(env.headers.map, [
                Accept           : "application/json",
                "Accept-language": "en-US"
        ])
        assertEquals(env.fixtures.operation, MetaListFactory.REPLACE)
        assertEquals(env.fixtures.list, ["sign", "token"])
    }
}
