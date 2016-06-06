package chihz.restgaga.meta

/**
 * TestSuite, 每个TestSuite封装了一组TestCase
 * 当执行此TestSuite时, 会自动执行包含的所有TestCase
 */
class TestSuiteMeta extends BaseMeta {

    /** 字符串集合, 可以包含testcase名称, 也可以包含testsuite名称 */
    def final List tests

    /** 是否并发执行 */
    def final boolean allConcurrent

    def TestSuiteMeta(String name, List tests, boolean allConcurrent) {
        super(name)
        this.tests = tests
        this.allConcurrent = allConcurrent
    }
}
