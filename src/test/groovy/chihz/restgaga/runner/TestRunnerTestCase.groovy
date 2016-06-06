package chihz.restgaga.runner

import chihz.restgaga.meta.loader.DSLMetaLoader
import chihz.restgaga.report.TextReportAction
import chihz.restgaga.test.mock.MockHttpService
import org.junit.*

import static org.junit.Assert.assertEquals

class TestRunnerTestCase {

    def static final MockHttpService mockHttpService = new MockHttpService(8098)

    @BeforeClass
    static void beforeClass() {
        mockHttpService.run()
    }

    @AfterClass
    static void afterClass() {
        mockHttpService.stopMock()
    }

    def final DSLMetaLoader dslMetaLoader = DSLMetaLoader.instance

    @Before
    void setUp() {

        new File("api_tests").mkdir()

        def envScript = """
        connectionTimeout = 500

        socketTimeout = 500

        globalHeaders (
            Accept: 'application/json'
        )
        env test {
            baseUrl 'http://localhost:8098'
        }
        env pro {
            baseUrl 'http://localhost:8098'
        }
        """

        new File("api_tests/env.groovy").write(envScript)

        def loginAPIScript = """
        api login {
            post '/v1/user/login'
            headers (
                "Content-Type": JSON_TYPE
            )
        }
        """

        new File("api_tests/login").mkdir()
        new File("api_tests/login/api.groovy").write(loginAPIScript)

        def loginTestsScript = """
        testcase login {
            success
            mapBody (
                username: 'samchi',
                password: '123456'
            )
            responseBody (
                code: 200,
                msg: Ignore
            )
        }

        testcase login {
            passwordErr
            mapBody (
                username: 'jackson',
                password: '123456'
            )
            responseBody (
                code: 400
            )
        }

        testcase login {
            exception
            mapBody (
                username: "samchi",
                password: "123456"
            )
            responseHeaders {
                throw new RuntimeException("Hehe!")
            }
        }
        """

        new File("api_tests/login/tests.groovy").write(loginTestsScript)

        def registerAPIScript = """
        api register {
            post '/v1/user/register'
            headers (
                "Content-Type": FORM_URL_TYPE
            )
        }
        """

        new File("api_tests/register").mkdir()
        new File("api_tests/register/api.groovy").write(registerAPIScript)

        def registerTestsScript = """
        testcase register {
            success
            mapBody (
                username: 'jackson',
                password: '123456',
                repassword: '123456',
                birthday: '1989-11-07'
            )
            responseBody (
                code: 200,
                msg: Ignore
            )
        }

        testcase register {
            userExisted
            mapBody (
                username: 'samchi',
                password: '123456',
                repassword: '123456',
                birthday: '1989-11-07'
            )
            responseBody (
                code: 400,
                msg: "user_existed"
            )
        }
        """

        new File("api_tests/register/tests.groovy").write(registerTestsScript)

        def suiteScipt = """
        testsuite registerThenLogin {
            test "login.success"
            test "register.success"
        }
        """

        new File("api_tests/register/suite.groovy").write(suiteScipt)

        def profileApiScript = """
        api profile {
            get '/v1/user/profile/{id}'
        }
        """

        new File("api_tests/profile").mkdir()
        new File("api_tests/profile/api.groovy").write(profileApiScript)

        def profileCaseWithTestEnv = """
        testcase profile {
            success
            pathVariables (
                id: 1
            )
            responseBody (
                user: [
                    id: 1,
                    username: "SamChi",
                    birthday: "1989-11-07"
                ],
                feeds: Ignore
            )
        }
        """

        new File("api_tests/profile/profile_test.test.groovy").write(profileCaseWithTestEnv)

        def profileCaseWithProEnv = """
        testcase profile {
            success
            pathVariables (
                id: 2
            )
            responseBody (
                user: [
                    id: 2,
                    username: "SamChi",
                    birthday: "1989-11-07"
                ],
                feeds: Ignore
            )
        }
        """

        new File("api_tests/profile/profile_test.pro.groovy").write(profileCaseWithProEnv)
    }

    @Test
    void testRunAPI() {
        TestRunner testRunner = new TestRunner("api_tests", "login")
        testRunner._loadMeta()
        TestSuiteResult result = testRunner._run()
        assertEquals(result.statusCounts[TestStatus.SUCCESS], 2)
        assertEquals(result.name, "login")
        new TextReportAction().report(result)
    }

    @Test
    void testRunTestCase() {
        TestRunner testRunner = new TestRunner("api_tests", "login.success")
        testRunner._loadMeta()
        TestSuiteResult result = testRunner._run()
        assertEquals(result.statusCounts[TestStatus.SUCCESS], 1)
        new TextReportAction().report(result)
    }

    @Test
    void testRunTestSuite() {
        TestRunner testRunner = new TestRunner("api_tests", "registerThenLogin")
        testRunner._loadMeta()
        TestSuiteResult result = testRunner._run()
        assertEquals(result.statusCounts[TestStatus.SUCCESS], 2)
        new TextReportAction().report(result)
    }

    @Test
    void testRunAll() {
        TestRunner testRunner = new TestRunner("api_tests", "all")
        testRunner._loadMeta()
        TestSuiteResult result = testRunner._run()
        assertEquals(result.statusCounts[TestStatus.SUCCESS], 5)
        new TextReportAction().report(result)
    }

    @Test
    void testDifferentEnv() {
        // change env to pro
        RuntimeProperties.instance.currentEnv = "pro"
        TestRunner testRunner = new TestRunner("api_tests", "profile.success")
        testRunner._loadMeta()
        TestSuiteResult result = testRunner._run()
        assertEquals(result.statusCounts[TestStatus.SUCCESS], 1)
        new TextReportAction().report(result)
    }

    @Test
    void testFullRun() {
        // 测试完整的运行流程
        TestRunner testRunner = new TestRunner("api_tests", "all")
        testRunner.run()
    }

    @After
    void tearDown() {
        RuntimeProperties.instance.currentEnv = "test"
        dslMetaLoader.cleanAll()
        new File("api_tests").deleteDir()
    }
}
