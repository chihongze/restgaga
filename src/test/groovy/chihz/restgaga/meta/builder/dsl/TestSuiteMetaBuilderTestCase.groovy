package chihz.restgaga.meta.builder.dsl

import chihz.restgaga.meta.TestSuiteMeta


class TestSuiteMetaBuilderTestCase extends DSLBuilderTestCase {

    def TestSuiteMetaBuilder testSuiteMetaBuilder = null

    void setUp() {
        testSuiteMetaBuilder = new TestSuiteMetaBuilder()
    }

    void testBuild() {
        TestSuiteMeta testsuite = testSuiteMetaBuilder.build {
            test "user.login"
            test "user.register"
        }
        assertEquals(testsuite.tests, ["user.login", "user.register"])
        assertFalse(testsuite.allConcurrent)
    }

    void testParallel() {
        TestSuiteMeta testsuite = testSuiteMetaBuilder.build {
            concurrent
            test "user.login"
            test "user.register"

        }
        assertEquals(testsuite.tests, ["user.login", "user.register"])
        assertTrue(testsuite.allConcurrent)
    }
}
