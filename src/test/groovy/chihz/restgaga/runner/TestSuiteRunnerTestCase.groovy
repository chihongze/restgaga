package chihz.restgaga.runner

import chihz.restgaga.meta.APIMeta
import chihz.restgaga.meta.TestSuiteMeta
import chihz.restgaga.meta.loader.DSLMetaLoader
import chihz.restgaga.meta.mgr.APIMetaManager
import chihz.restgaga.meta.mgr.TestSuiteMetaManager
import chihz.restgaga.report.TextReportAction
import chihz.restgaga.test.mock.MockHttpService
import org.junit.*

import static org.junit.Assert.assertEquals

class TestSuiteRunnerTestCase {

    def static final MockHttpService mockHttpService = new MockHttpService(8092)

    @BeforeClass
    static void beforeClass() {
        mockHttpService.run()
    }

    @AfterClass
    static void afterClass() {
        mockHttpService.stopMock()
    }

    def final DSLMetaLoader dslMetaLoader = DSLMetaLoader.instance

    def final TestSuiteMetaManager testSuiteMetaMgr = TestSuiteMetaManager.instance

    def final APIMetaManager apiMetaManager = APIMetaManager.instance

    @Before
    void setUp() {
        def script = """

        env test {
            baseUrl 'http://localhost:8092'
        }

        api login {
            post '/v1/user/login'
            headers (
                "Content-Type": JSON_TYPE
            )
        }

        testcase login {
            success
            mapBody (
                username: "samchi",
                password: "123456"
            )
        }

        testcase login {
            passwordErr
            mapBody (
                username: "jackson",
                password: "123456"
            )
            responseBody (
                code: 200,
                msg: Ignore
            )
        }

        testcase login {
            successWithHeaders
            mapBody (
                username: "samchi",
                password: "123456"
            )
            responseHeaders (
                "sign": "SIGN",
                "token": "TOKEN"
            )
        }

        testsuite loginSuccessSuites {
            concurrent
            test "login.success"
            test "login.successWithHeaders"
        }

        testsuite loginSuites {
            test "loginSuccessSuites"
            test "login.passwordErr"
        }

        testsuite loginSuitesParallel {
            concurrent
            test "loginSuccessSuites"
            test "login.passwordErr"
        }

        """
        dslMetaLoader.load(script)
    }

    @Test
    void testCommonSuites() {
        TestSuiteMeta testSuite = testSuiteMetaMgr.getMetaObject("loginSuites")
        TestSuiteRunner testSuiteRunner = new TestSuiteRunner(testSuite)
        TestSuiteResult result = testSuiteRunner.run()
        assertEquals(result.name, "loginSuites")
        EnumMap statusCounts = new EnumMap(TestStatus.class)
        for (TestStatus status in TestStatus.values()) {
            statusCounts.put(status, 0)
        }
        statusCounts.put(TestStatus.BODY_NO_MATCH, 1)
        statusCounts.put(TestStatus.SUCCESS, 2)
        assertEquals(result.statusCounts, statusCounts)
        new TextReportAction().report(result)
    }

    @Test
    void testConcurrentSuites() {
        TestSuiteMeta testSuite = testSuiteMetaMgr.getMetaObject("loginSuitesParallel")
        TestSuiteRunner testSuiteRunner = new TestSuiteRunner(testSuite)
        TestSuiteResult result = testSuiteRunner.run()
        assertEquals(result.name, "loginSuitesParallel")
        EnumMap statusCounts = new EnumMap(TestStatus.class)
        for (TestStatus status in TestStatus.values()) {
            statusCounts.put(status, 0)
        }
        statusCounts.put(TestStatus.BODY_NO_MATCH, 1)
        statusCounts.put(TestStatus.SUCCESS, 2)
        assertEquals(result.statusCounts, statusCounts)
        new TextReportAction().report(result)
    }

    @Test
    void testAPIMeta() {
        APIMeta apiMeta = apiMetaManager.getMetaObject("login")
        TestSuiteRunner testSuiteRunner = new TestSuiteRunner(apiMeta)
        TestSuiteResult testSuiteResult = testSuiteRunner.run()
        assertEquals(testSuiteResult.name, "login")
        EnumMap statusCounts = new EnumMap(TestStatus.class)
        for (TestStatus status in TestStatus.values()) {
            statusCounts.put(status, 0)
        }
        statusCounts.put(TestStatus.BODY_NO_MATCH, 1)
        statusCounts.put(TestStatus.SUCCESS, 2)
        assertEquals(testSuiteResult.statusCounts, statusCounts)
        new TextReportAction().report(testSuiteResult)
    }

    @After
    void tearDown() {
        this.dslMetaLoader.cleanAll()
    }
}
