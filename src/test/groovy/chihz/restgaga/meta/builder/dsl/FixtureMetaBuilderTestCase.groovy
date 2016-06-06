package chihz.restgaga.meta.builder.dsl


class FixtureMetaBuilderTestCase extends DSLBuilderTestCase {

    def FixtureMetaBuilder builder = null

    void setUp() {
        this.builder = new FixtureMetaBuilder()
    }

    void testBuild() {
        def signFixture = this.builder.build sign {
            setUp {
                "setUp"
            }
            tearDown {
                "tearDown"
            }
        }
        assertEquals(signFixture.name, "sign")
        assertEquals(signFixture.setUp(), "setUp")
        assertEquals(signFixture.tearDown(), "tearDown")
    }
}
