package chihz.restgaga.meta.builder.dsl

import chihz.restgaga.meta.TestSuiteMeta

/**
 * 该Builder可以通过以下形式的DSL来构建TestSuite对象
 *
 * <pre>
 *     builder = new TestSuiteBuilder()
 *     builder my_testsuite {*         concurrent
 *         test "user.login"
 *         test "user.register"
 *         test "user.settings"
 *         test "usersuites"
 *}* </pre>
 */
class TestSuiteMetaBuilder extends BaseDSLBuilder {

    def List tests = []

    def boolean allConcurrent = false


    def test(String test) {
        this.tests.add(test)
        this
    }

    def allConcurrent(boolean concurrent) {
        this.allConcurrent = concurrent
        this
    }

    def propertyMissing(String name) {
        if (name == "concurrent") {
            this.allConcurrent = true
        }
    }

    def newInstance() {
        new TestSuiteMeta(this.name, this.tests, this.allConcurrent)
    }
}
