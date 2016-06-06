package chihz.restgaga.meta.builder.dsl

import chihz.restgaga.meta.APIMeta
import chihz.restgaga.meta.MetaListFactory
import chihz.restgaga.meta.RequestMethod


class APIMetaBuilderTestCase extends DSLBuilderTestCase {

    def APIMetaBuilder builder = null

    def Expando mockCtx = null

    void setUp() {
        builder = new APIMetaBuilder()
        mockCtx = new Expando()
    }

    void testBuild() {
        APIMeta apiMeta = builder.build login {
            post '/v1/user/login'
            headers(
                    Accept: "application/json"
            )
            params(
                    username: "samchi",
                    password: "123456"
            )
            pathVariables(

            )
            fixtures ^ ["sign"]
        }

        assertEquals(apiMeta.name, "login")
        assertEquals(apiMeta.method, RequestMethod.POST)
        assertEquals(apiMeta.headers.map, [Accept: "application/json"])
        assertEquals(apiMeta.params.map, [username: "samchi", password: "123456"])
        assertEquals(apiMeta.fixtures.list, ["sign"])
        assertEquals(apiMeta.fixtures.operation, MetaListFactory.REPLACE)
    }
}
