package chihz.restgaga.runner

import chihz.restgaga.meta.NotEmpty
import chihz.restgaga.meta.Regexp
import chihz.restgaga.meta.Size
import chihz.restgaga.meta.TestCaseMeta
import chihz.restgaga.meta.loader.DSLMetaLoader
import chihz.restgaga.meta.mgr.TestCaseMetaManager
import chihz.restgaga.test.mock.MockHttpService
import com.mashape.unirest.http.HttpResponse
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*

import static org.junit.Assert.*

class DoRequestAndCompareTestCase {

    def static MockHttpService httpService = new MockHttpService(8090)

    def DSLMetaLoader metaLoader = DSLMetaLoader.instance

    def TestCaseMetaManager testCaseMetaManager = TestCaseMetaManager.instance

    def File testFile = null

    @BeforeClass
    public static void beforeClass() {
        httpService.run()
    }

    @AfterClass
    public static void afterClass() {
        httpService.stopMock()
    }

    @Before
    void setUp() {
        def script = """

        global.username = "samchi"

        globalHeaders (
            "Accept": "application/json"
        )

        env test {
            baseUrl 'http://localhost:8090'
        }

        api login {
            post '/v1/user/login'
            headers (
                "Content-Type": JSON_TYPE
            )
        }

        api register {
            post '/v1/user/register'
            headers (
                "Content-Type": FORM_URL_TYPE
            )
        }

        api viewProfile {
            get '/v1/user/profile/{userId}'
        }

        api uploadFile {
            put '/v1/pan/upload'
        }

        testcase login {
            success
            mapBody (
                username: "samchi",
                password: "123456",
            )
            responseHeaders (
                sign: "sign",
                token: "token"
            )
        }

        testcase login {
            error_headers
            mapBody (
                username: "samchi",
                password: "123456"
            )
            responseHeaders {
                [x: "x", y: "y"]
            }
        }

        testcase login {
            password_error
            mapBody (
                username: "samchi",
                password: "654321"
            )
            responseHeaders (
                x: "x"
            )
        }

        testcase register {
            success
            mapBody (
                username: "jackson",
                password: "123456",
                repassword: "123456",
                birthday: "1989-11-07"
            )
        }

        testcase register {
            user_existed
            mapBody {
                [
                    username: global.username,
                    password: "123456",
                    repassword: "123456",
                    birthday: "1989-11-07"
                ]
            }
        }

        testcase viewProfile {
            user_1
            pathVariables (
                userId: 1
            )
        }

        testcase viewProfile {
            user_3800
            pathVariables {
                [userId: 3800]
            }
        }

        testcase uploadFile {
            testfile
            fileBody 'testfile.txt'
        }

        testcase uploadFile {
            bytes
            bytesBody '{"name": "SamChi"}'.bytes
        }

        testcase uploadFile {
            closure
            bytesBody {
                '{"name": "SamChi"}'.bytes
            }
        }
        """
        metaLoader.load(script)

        testFile = new File("testfile.txt")
        testFile.write("{\"name\": \"SamChi\"}")
    }

    @Test
    void testJSONBodyRequest() {
        JSONObject responseJSON = _makeResponse("login.success")
        assertEquals(responseJSON.getInt("code"), 200)

        responseJSON = _makeResponse("login.password_error")
        assertEquals(responseJSON.getInt("code"), 400)
        assertEquals(responseJSON.getString("msg"), "login_error")
    }

    @Test
    void testFormURLBodyRequest() {
        JSONObject responseJSON = _makeResponse("register.success")
        assertEquals(responseJSON.getInt("code"), 200)
        assertEquals(responseJSON.getJSONObject("data").getString("username"), "jackson")

        responseJSON = _makeResponse("register.user_existed")
        assertEquals(responseJSON.getInt("code"), 400)
        assertEquals(responseJSON.getString("msg"), "user_existed")
    }

    @Test
    void testPathVariableRequest() {
        JSONObject responseJSON = _makeResponse("viewProfile.user_1")
        assertEquals(responseJSON.getJSONObject("user").getInt("id"), 1)

        responseJSON = _makeResponse("viewProfile.user_3800")
        assertEquals(responseJSON.getJSONObject("user").getInt("id"), 3800)
    }

    @Test
    void testBytesBodyRequest() {
        JSONObject responseJSON = _makeResponse("uploadFile.testfile")
        assertEquals(responseJSON.getString("name"), "SamChi")

        responseJSON = _makeResponse("uploadFile.bytes")
        assertEquals(responseJSON.getString("name"), "SamChi")

        responseJSON = _makeResponse("uploadFile.closure")
        assertEquals(responseJSON.getString("name"), "SamChi")
    }

    @Test
    void testCompareValue() {
        TestCaseRunner testCaseRunner = _getTestRunner("login.success")

        // expected ignore
        DifferentItem diffItem = testCaseRunner._compareValue("test",
                chihz.restgaga.meta.Ignore.instance, null)
        assertNull(diffItem)

        // expected not empty
        diffItem = testCaseRunner._compareValue("test", NotEmpty.instance, null)
        assertEquals(diffItem.key, "test")
        assertEquals(diffItem.expectedValue, NotEmpty.instance)
        assertEquals(diffItem.actualValue, "")

        diffItem = testCaseRunner._compareValue("test", NotEmpty.instance, "")
        assertNotNull(diffItem)

        // expected range
        diffItem = testCaseRunner._compareValue("test", 1..5, "2")
        assertNull(diffItem)
        diffItem = testCaseRunner._compareValue("test", 1..5, "10")
        assertNotNull(diffItem)

        // expected string
        diffItem = testCaseRunner._compareValue("test", "a", "a")
        assertNull(diffItem)
        diffItem = testCaseRunner._compareValue("test", "a", "A")
        assertEquals(diffItem.key, "test")
        assertEquals(diffItem.expectedValue, "a")
        assertEquals(diffItem.actualValue, "A")

        // expected string ignore case
        diffItem = testCaseRunner._compareValue("test", "aaa", "AAA", true)
        assertNull(diffItem)
        diffItem = testCaseRunner._compareValue("test", "aba", "AAA", true)
        assertNotNull(diffItem)

        // expected regex
        diffItem = testCaseRunner._compareValue("test", new Regexp(/\d+/, false), "1111aaa")
        assertNull(diffItem)
        diffItem = testCaseRunner._compareValue("test", new Regexp(/\d+/, true), "11111aaa")
        assertNotNull(diffItem)
    }

    @Test
    void testCompareHeaders() {
        TestCaseRunner testCaseRunner = this._getTestRunner("login.success")
        _prepareRequest(testCaseRunner)
        HttpResponse response = testCaseRunner._doRequest()
        TestCaseResult result = testCaseRunner._compareHeaders(response, 0, 0)
        assertEquals(result.status, TestStatus.SUCCESS)

        testCaseRunner = this._getTestRunner("login.password_error")
        _prepareRequest(testCaseRunner)
        response = testCaseRunner._doRequest()
        result = testCaseRunner._compareHeaders(response, 0, 0)
        assertEquals(result.differentHeaderItems[0].key, "x")
        assertEquals(result.differentHeaderItems[0].actualValue, "")
        assertEquals(result.differentHeaderItems[0].expectedValue, "x")

        testCaseRunner = this._getTestRunner("login.error_headers")
        _prepareRequest(testCaseRunner)
        response = testCaseRunner._doRequest()
        result = testCaseRunner._compareHeaders(response, 0, 0)
        assertEquals(result.differentHeaderItems.size(), 2)
        assertEquals(result.expectedHeaders, [x: "x", y: "y"])
    }

    @Test
    void testCompareBody() {
        TestCaseRunner testCaseRunner = this._getTestRunner("login.success")

        // expected ignore
        DifferentItem diffItem = testCaseRunner._compareBody("ROOT", chihz.restgaga.meta.Ignore.instance, null)
        assertNull(diffItem)

        // expected not_empty
        diffItem = testCaseRunner._compareBody("ROOT", NotEmpty.instance, "")
        assertEquals(diffItem.key, "ROOT")
        assertEquals(diffItem.expectedValue, NotEmpty.instance)
        assertEquals(diffItem.actualValue, "")

        diffItem = testCaseRunner._compareBody("ROOT", NotEmpty.instance, "hehe")
        assertNull(diffItem)

        // expected basic type
        diffItem = testCaseRunner._compareBody("ROOT", 1, "1")
        assertNull(diffItem)
        diffItem = testCaseRunner._compareBody("ROOT", new Regexp(/\d+/), "12345a")
        assertEquals(diffItem.actualValue, "12345a")

        // expected map
        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [id: 1, name: "samchi"],
                new JSONObject([id: "1", name: "samchi", birth: "1989-11-07"])
        )
        assertNull(diffItem)

        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [id: 1, name: "jackson"],
                new JSONObject([id: "1", name: "samchi", birth: "1989-11-07"])
        )
        assertEquals(diffItem.key, "ROOT.name")
        assertEquals(diffItem.expectedValue, "jackson")
        assertEquals(diffItem.actualValue, "samchi")

        // ignore in map
        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [id: 1, name: chihz.restgaga.meta.Ignore.instance],
                new JSONObject([id: "1", name: "samchi", birth: "1989-11-07"])
        )
        assertNull(diffItem)

        // nested map
        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [code: 200, data: [id: "1", name: [first: "Sam", last: "Chi"]]],
                new JSONObject([code: 200, data: [id: "1", name: [first: "Sam", last: "Chi"]]])
        )
        assertNull(diffItem)

        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [code: 200, data: [id: "1", name: [first: "Jack", last: "Chi"]]],
                new JSONObject([code: 200, data: [id: "1", name: [first: "Sam", last: "Chi"]]])
        )
        assertEquals(diffItem.key, "ROOT.data.name.first")
        assertEquals(diffItem.expectedValue, "Jack")
        assertEquals(diffItem.actualValue, "Sam")

        // list in map
        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [code: 200, data: [
                        [id: 1, name: "小明", score: 98],
                        [id: 2, name: "小红", score: 86]
                ]],
                new JSONObject([code: 200, data: [
                        [id: 1, name: "小明", score: 98],
                        [id: 2, name: "小红", score: 86]
                ]])
        )
        assertNull(diffItem)

        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [code: 200, data: [
                        [id: 1, name: "小明", score: 98],
                        [id: 2, name: "小红", score: 86]
                ]],
                new JSONObject([code: 200, data: [
                        [id: 1, name: "小明", score: 98],
                        [id: 3, name: "小华", score: 76]
                ]])
        )
        assertEquals(diffItem.key, "ROOT.data[1].id")
        assertEquals(diffItem.expectedValue, "2")
        assertEquals(diffItem.actualValue, "3")

        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [code: 200, data: [NotEmpty.instance]],
                new JSONObject([code: 200, data: []])
        )
        assertEquals(diffItem.key, "ROOT.data")
        assertEquals(diffItem.expectedValue, [NotEmpty.instance])

        // list
        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [
                        [id: 1, name: "Sam"],
                        [id: 2, name: "Jack"],
                        [id: 3, name: "Tom"]
                ],
                new JSONArray([
                        [id: 1, name: "Sam"],
                        [id: 2, name: "Jack"],
                        [id: 3, name: "Tom"]
                ])
        )
        assertNull(diffItem)

        // size
        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [
                        [id: 1, name: "Sam"],
                        [id: 2, name: "Jack"],
                        [id: 3, name: "Tom"]
                ],
                new JSONArray([
                        [id: 1, name: "Sam"],
                        [id: 2, name: "Jack"]
                ])
        )

        assertEquals(diffItem.key, "ROOT")
        assertEquals(diffItem.expectedValue, [
                [id: 1, name: "Sam"],
                [id: 2, name: "Jack"],
                [id: 3, name: "Tom"]
        ])

        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [
                        [id: 1, name: "Sam"],
                        new Size(2)
                ],
                new JSONArray([
                        [id: 1, name: "Sam"],
                        [id: 2, name: "Jack"]
                ])
        )

        assertNull(diffItem)

        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [
                        [id: 1, name: "Sam"],
                        new Size(2)
                ],
                new JSONArray([
                        [id: 1, name: "Sam"]
                ])
        )

        assertEquals(diffItem.key, "ROOT")

        // basic type
        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [1, "2", 3, 4, new BigDecimal("22.34")],
                ["1", "2", "3", "4", "22.34"]
        )
        assertNull(diffItem)

        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [1, "2", 3, 4, new BigDecimal("22.34")],
                ["1", "2", "3", "4", "22.35"]
        )
        assertEquals(diffItem.key, "ROOT[4]")
        assertEquals(diffItem.expectedValue, "22.34")
        assertEquals(diffItem.actualValue, "22.35")

        // map in list
        diffItem = testCaseRunner._compareBody(
                "ROOT",
                [
                        [id: 1, name: "Sam"],
                        [id: 2, name: "Jack"]
                ],
                [
                        [id: 1, name: "Sam"],
                        [id: 3, name: "Jack"]
                ]
        )

        assertEquals(diffItem.key, "ROOT[1].id")
        assertEquals(diffItem.expectedValue, "2")
        assertEquals(diffItem.actualValue, "3")
    }

    def _getTestRunner(String testCaseName) {
        TestCaseMeta testCaseMeta = testCaseMetaManager.getMetaObject(testCaseName)
        return new TestCaseRunner(testCaseMeta)
    }

    def JSONObject _makeResponse(String testCaseName) {
        TestCaseRunner testCaseRunner = _getTestRunner(testCaseName)
        _prepareRequest(testCaseRunner)

        HttpResponse response = testCaseRunner._doRequest()
        return new JSONObject(response.body)
    }

    void _prepareRequest(TestCaseRunner testCaseRunner) {
        testCaseRunner._buildHeaders()
        testCaseRunner._buildParams()
        testCaseRunner._buildPathVariables()
        testCaseRunner._buildBody()
    }

    @After
    void tearDown() {
        this.metaLoader.cleanAll()
        testFile.delete()
    }
}
