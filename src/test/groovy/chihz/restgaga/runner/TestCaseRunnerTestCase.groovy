package chihz.restgaga.runner

import chihz.restgaga.meta.GlobalNamespace
import chihz.restgaga.meta.TestCaseMeta
import chihz.restgaga.meta.loader.DSLMetaLoader
import chihz.restgaga.meta.mgr.TestCaseMetaManager
import chihz.restgaga.report.TextReportAction
import chihz.restgaga.test.mock.MockHttpService
import org.junit.*

import static org.junit.Assert.assertEquals

class TestCaseRunnerTestCase {

    def static final MockHttpService httpService = new MockHttpService(8091)

    def final DSLMetaLoader dslMetaLoader = DSLMetaLoader.instance

    def final TestCaseMetaManager testCaseMetaManager = TestCaseMetaManager.instance

    def final GlobalNamespace global = GlobalNamespace.instance

    @BeforeClass
    static void beforeClass() {
        httpService.run()
    }

    @AfterClass
    static void afterClass() {
        httpService.stopMock()
    }

    @Before
    void setUp() {
        def script = """

        global.count = 0

        globalHeaders (
            Accept: "application/json"
        )

        fixture a {

            setUp {
                headers["a"] = "A"
            }

            tearDown {
                global.count ++
            }
        }

        fixture b {

            setUp {
                headers["b"] = "B"
            }

            tearDown {
                global.count ++
            }

        }

        fixture c {

            setUp {
                headers["c"] = "C"
            }

            tearDown {
                global.count ++
            }

        }

        globalFixtures << [ "a" ]

        env test {
            baseUrl 'http://localhost:8091'
            headers (
                "Content-Type": JSON_TYPE
            )
        }

        api login {
            post '/v1/user/login'
        }

        api viewProfile {
            get '/v1/user/profile/{userId}'
        }

        api echoHeaders {
            get '/v1/test/echoHeaders'
            fixtures << [ "b" ]
        }

        testcase login {
            success

            headers (
                "Content-Type": JSON_TYPE
            )

            mapBody (
                username: "samchi",
                password: "123456"
            )

            responseHeaders (
                sign: "SIGN",
                token: "TOKEN"
            )

            responseBody (
                code: regexp("([0-9])+"),
                msg: predict({it == "success"})
            )
        }

        testcase login {
            success_with_groovy

            mapBody {
                [ username: "samchi", password: "123456" ]
            }

            responseHeaders {
                [ sign: "SIGN", "token": "TOKEN" ]
            }

            responseBody {
                [ code: 200, msg: "success" ]
            }
        }

        testcase login {

            login_400_success

            status 400

            mapBody (
                username: "hehe",
                password: "123456"
            )
        }

        testcase login {

            login_400_error

            status 404

            mapBody (
                username: "hehe",
                password: "123456"
            )
        }

        testcase login {

            headers_no_match

            mapBody (
                username: "samchi",
                password: "123456"
            )

            responseHeaders (
                x: "xxxx",
                y: "yyyy",
                token: "tttt"
            )
        }

        testcase login {

            body_no_match

            mapBody (
               username: "samchi",
               password: "123456"
            )

            responseBody (
                code: 400,
                msg: Ignore
            )
        }

        testcase viewProfile {

            success

            pathVariables (
                userId: 1001
            )

            responseBody (
                user: [
                    id: 1001,
                    username: "SamChi",
                    birthday: "1989-11-07"
                ],
                feeds: [
                    [ feedId: NotEmpty, content: NotEmpty ],
                    Size(2)
                ]
            )
        }

        testcase echoHeaders {

            success

            fixtures << [ "c" ]

            setUp {
                headers["d"] = "D"
            }

            responseBody (
                a: "A",
                b: "B",
                c: "C",
                d: "D"
            )

            tearDown {
                global.count ++
            }
        }
        """

        dslMetaLoader.load(script)
    }

    @Test
    void testSuccess() {
        TestCaseRunner testCaseRunner = this._getTestCaseRunner("login.success")
        TestCaseResult testCaseResult = testCaseRunner.run()
        assertEquals(testCaseResult.status, TestStatus.SUCCESS)
        println new TextReportAction().printTestCase(testCaseResult, 0)

        testCaseRunner = this._getTestCaseRunner("login.success_with_groovy")
        testCaseResult = testCaseRunner.run()
        assertEquals(testCaseResult.status, TestStatus.SUCCESS)
        println new TextReportAction().printTestCase(testCaseResult, 0)
    }

    @Test
    void testStatusMatch() {
        TestCaseRunner testCaseRunner = this._getTestCaseRunner("login.login_400_success")
        TestCaseResult testCaseResult = testCaseRunner.run()
        assertEquals(testCaseResult.status, TestStatus.SUCCESS)
        println new TextReportAction().printTestCase(testCaseResult, 0)

        testCaseRunner = this._getTestCaseRunner("login.login_400_error")
        testCaseResult = testCaseRunner.run()
        assertEquals(testCaseResult.status, TestStatus.STATUS_NO_MATCH)
        assertEquals(testCaseResult.actualStatus, 400)
        assertEquals(testCaseResult.expectedStatus, 404)
        println new TextReportAction().printTestCase(testCaseResult, 0)
    }

    @Test
    void testHeadersMatch() {
        TestCaseRunner testCaseRunner = this._getTestCaseRunner("login.headers_no_match")
        TestCaseResult testCaseResult = testCaseRunner.run()
        assertEquals(testCaseResult.status, TestStatus.HEADER_NO_MATCH)
        assertEquals(testCaseResult.differentHeaderItems.size(), 3)
        assertEquals(testCaseResult.differentHeaderItems[0].key, "x")
        assertEquals(testCaseResult.differentHeaderItems[0].expectedValue, "xxxx")
        println new TextReportAction().printTestCase(testCaseResult, 0)
    }

    @Test
    void testBodyMatch() {
        TestCaseRunner testCaseRunner = this._getTestCaseRunner("login.body_no_match")
        TestCaseResult testCaseResult = testCaseRunner.run()
        assertEquals(testCaseResult.status, TestStatus.BODY_NO_MATCH)
        assertEquals(testCaseResult.differentBodyItem.key, "ROOT.code")
        assertEquals(testCaseResult.differentBodyItem.expectedValue, "400")
        println new TextReportAction().printTestCase(testCaseResult, 0)

        testCaseRunner = this._getTestCaseRunner("viewProfile.success")
        testCaseResult = testCaseRunner.run()
        assertEquals(testCaseResult.status, TestStatus.SUCCESS)
        println new TextReportAction().printTestCase(testCaseResult, 0)
    }


    @Test
    void testFixturesAndSetUp() {
        TestCaseRunner testCaseRunner = this._getTestCaseRunner("echoHeaders.success")
        TestCaseResult testCaseResult = testCaseRunner.run()
        println testCaseResult.differentBodyItem
        assertEquals(testCaseResult.status, TestStatus.SUCCESS)
        assertEquals(global.count, 4)
        println new TextReportAction().printTestCase(testCaseResult, 0)
    }

    TestCaseRunner _getTestCaseRunner(String testCaseName) {
        TestCaseMeta testCaseMeta = testCaseMetaManager.getMetaObject(testCaseName)
        return new TestCaseRunner(testCaseMeta)
    }

    @After
    void tearDown() {
        dslMetaLoader.cleanAll()
    }

}
