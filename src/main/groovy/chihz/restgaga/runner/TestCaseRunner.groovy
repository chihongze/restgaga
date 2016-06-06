package chihz.restgaga.runner

import chihz.restgaga.meta.*
import chihz.restgaga.meta.mgr.APIMetaManager
import chihz.restgaga.meta.mgr.EnvMetaManager
import chihz.restgaga.meta.mgr.FixtureMetaManager
import chihz.restgaga.meta.mgr.MetaObjectNotExistedException
import chihz.restgaga.util.Lang
import com.mashape.unirest.http.Headers
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.request.HttpRequest
import com.mashape.unirest.request.HttpRequestWithBody
import org.apache.commons.collections.map.CaseInsensitiveMap
import org.json.JSONArray
import org.json.JSONObject

public class TestCaseRunner extends Runner {

    def final runtimeProperties = RuntimeProperties.instance

    def final EnvMeta envMeta

    def final APIMeta apiMeta

    def final TestCaseMeta testCaseMeta

    def final TestCaseContext testCaseContext

    def final String testCaseFullName

    def final fixtures

    def final String fullUrl

    def TestCaseRunner(TestCaseMeta testCaseMeta) {
        this.testCaseMeta = testCaseMeta
        this.envMeta = EnvMetaManager.instance.getMetaObject(runtimeProperties.currentEnv)
        this.apiMeta = APIMetaManager.instance.getMetaObject(testCaseMeta.api)
        this.testCaseContext = new TestCaseContext(runtimeProperties.currentEnv)
        this.testCaseFullName = "${apiMeta.name}.${testCaseMeta.name}"
        this.fixtures = _getFixtures()
        this.fullUrl = new URL(new URL(envMeta.baseUrl), apiMeta.path).toString()
    }

    def TestCaseResult run() {
        long beginTime = System.currentTimeMillis()
        long requestTime = 0
        TestCaseResult result
        try {
            _buildHeaders()
            _buildParams()
            _buildPathVariables()
            _buildBody()

            _runFixturesSetUp()
            _runSetUp()

            requestTime = System.currentTimeMillis()
            HttpResponse response = _doRequest()
            requestTime = System.currentTimeMillis() - requestTime

            result = _compareResponse(response, beginTime, requestTime)

            _runTearDown()
            _runFixturesTearDown()
            return result
        } catch (Throwable e) {
            e.printStackTrace()
            long usedTime = System.currentTimeMillis() - beginTime
            result = new TestCaseResult(
                    testCaseFullName, TestStatus.EXCEPTION, usedTime,
                    requestTime, 0, 0, null, null, null, null, null, null,
                    e
            )
            return result
        }
    }

    def _getFixtures() {
        def fixtureMetaMgr = FixtureMetaManager.instance
        List fixtureNames = MetaListFactory.getFinallyList(
                testCaseContext,
                runtimeProperties.globalFixtures,
                envMeta.fixtures,
                apiMeta.fixtures,
                testCaseMeta.fixtures
        )
        fixtureNames.collect({
            FixtureMeta fixtureMeta = fixtureMetaMgr.getMetaObject(it)
            if (fixtureMeta == null) {
                throw new MetaObjectNotExistedException(
                        "Fixture '$it' not existed in testcase '$testCaseFullName'"
                )
            }
            fixtureMeta
        })
    }

    def _buildHeaders() {
        testCaseContext.updateHeaders(
                runtimeProperties.globalHeaders,
                envMeta.headers,
                apiMeta.headers,
                testCaseMeta.headers
        )
    }

    def _buildParams() {
        testCaseContext.updateParams(
                apiMeta.params,
                testCaseMeta.params
        )
    }

    def _buildPathVariables() {
        testCaseContext.updatePathVariables(
                apiMeta.pathVariables,
                testCaseMeta.pathVariables
        )
    }

    def _buildBody() {
        if (testCaseMeta.body == null) {
            return
        }

        def body = testCaseMeta.body.getBody(this.testCaseContext)
        this.testCaseContext.body = body
    }

    def _runFixturesSetUp() {
        this.fixtures.each({
            if (it.setUp != null) {
                Lang.cloneAndRun(it.setUp, testCaseContext)
            }
        })
    }

    def _runSetUp() {
        if (testCaseMeta.setUp == null) {
            return
        }
        Lang.cloneAndRun(testCaseMeta.setUp, testCaseContext)
    }

    def HttpResponse _doRequest() {
        HttpRequest httpReq = null

        if (apiMeta.method == RequestMethod.GET) {
            httpReq = Unirest.get(fullUrl)
        } else if (apiMeta.method == RequestMethod.POST) {
            httpReq = Unirest.post(fullUrl)
        } else if (apiMeta.method == RequestMethod.PUT) {
            httpReq = Unirest.put(fullUrl)
        } else if (apiMeta.method == RequestMethod.DELETE) {
            httpReq = Unirest.delete(fullUrl)
        }

        // add path variables
        if (testCaseContext.pathVariables) {
            testCaseContext.pathVariables.each({ k, v ->
                httpReq.routeParam(k, v.toString())
            })
        }

        // add headers
        if (testCaseContext.headers) {
            httpReq.headers(testCaseContext.headers)
        }

        // request queryStrings
        if (testCaseContext.params) {
            httpReq.queryString(testCaseContext.params)
        }

        // request with body
        if ((httpReq instanceof HttpRequestWithBody) && (testCaseMeta.body != null)) {
            HttpRequestWithBody httpBodyReq = (HttpRequestWithBody) httpReq
            String contentType = testCaseContext.headers.get("Content-Type")

            RequestBodyType bodyType = testCaseMeta.body.type

            if (contentType != null && contentType.toLowerCase().contains("json")) {
                // application/json content type
                JSONObject jsonBody
                if (bodyType == RequestBodyType.Bytes) {
                    String jsonContent = new String((byte[]) testCaseContext.body)
                    jsonBody = new JSONObject(jsonContent)
                } else {
                    jsonBody = new JSONObject((Map) testCaseContext.body)
                }

                httpBodyReq.body(jsonBody)
            } else {
                // application/x-www-form-urlencoded or other types
                if (bodyType == RequestBodyType.Bytes) {
                    httpBodyReq.body((byte[]) testCaseContext.body)
                } else {
                    httpBodyReq.fields((Map) testCaseContext.body)
                }
            }
        }
        return httpReq.asString()
    }

    def _runTearDown() {
        if (testCaseMeta.tearDown == null) {
            return
        }
        Lang.cloneAndRun(testCaseMeta.tearDown, testCaseContext)
    }

    def _runFixturesTearDown() {
        this.fixtures.each({
            if (it.tearDown != null) {
                Lang.cloneAndRun(it.tearDown, testCaseContext)
            }
        })
    }

    def TestCaseResult _compareResponse(HttpResponse response, long beginTime, long requestTime) {
        // compare status
        TestCaseResult result = _compareStatus(response, beginTime, requestTime)
        if (result) {
            return result
        }

        // compare http headers
        TestCaseResult headerResult = _compareHeaders(response, beginTime, requestTime)
        if (headerResult.status == TestStatus.HEADER_NO_MATCH) {
            return headerResult
        }

        // compare json result
        JSONObject actualJSONBody = new JSONObject("{}")


        Map expectedBody = testCaseMeta.responseBody.getMap(testCaseContext)
        if (expectedBody) {

            try {
                actualJSONBody = new JSONObject(response.body)
            } catch (e) {
                throw new RuntimeException("API json response body parse error! Body: ${response.body}", e)
            }

            DifferentItem diffBodyItem = this._compareBody("ROOT", expectedBody, actualJSONBody)
            // 有情况!
            if (diffBodyItem != null) {
                return new TestCaseResult(
                        testCaseFullName,
                        TestStatus.BODY_NO_MATCH,
                        System.currentTimeMillis() - beginTime,
                        requestTime,
                        testCaseMeta.status,
                        response.status,
                        headerResult.expectedHeaders,
                        headerResult.actualHeaders,
                        Collections.emptyList(),
                        expectedBody,
                        actualJSONBody,
                        diffBodyItem,
                        null
                )
            }
        }

        // Success
        return new TestCaseResult(
                testCaseFullName,
                TestStatus.SUCCESS,
                System.currentTimeMillis() - beginTime,
                requestTime,
                testCaseMeta.status,
                response.status,
                headerResult.expectedHeaders,
                headerResult.actualHeaders,
                Collections.emptyList(),
                expectedBody,
                actualJSONBody,
                null,
                null
        )
    }

    def _handleHeaders(Headers headers) {
        Map result = new CaseInsensitiveMap(headers.size())
        headers.each({ k, _ ->
            result.put(k, headers.getFirst(k))
        })
        result
    }

    def _compareStatus(HttpResponse response, long beginTime, long requestTime) {
        // compare status
        if (testCaseMeta.status > 0 && testCaseMeta.status != response.status) {
            return new TestCaseResult(
                    testCaseFullName,
                    TestStatus.STATUS_NO_MATCH,
                    System.currentTimeMillis() - beginTime,
                    requestTime,
                    testCaseMeta.status,
                    response.status
            )
        }
        return null
    }

    def _compareHeaders(HttpResponse response, long beginTime, long requestTime) {
        Map expectedHeaders = testCaseMeta.responseHeaders.getMap(testCaseContext)
        if (expectedHeaders) {
            List differentHeaderItems = []
            Map actualHeaders = _handleHeaders(response.headers)
            for (Map.Entry entry in expectedHeaders.entrySet()) {
                String expectedKey = entry.getKey()
                def expectedVal = entry.getValue()
                String actualVal = actualHeaders.get(expectedKey)
                DifferentItem differentItem = this._compareValue(expectedKey, expectedVal, actualVal, true)
                if (differentItem) {
                    differentHeaderItems.add(differentItem)
                }
            }

            if (differentHeaderItems) {
                return new TestCaseResult(
                        testCaseFullName,
                        TestStatus.HEADER_NO_MATCH,
                        System.currentTimeMillis() - beginTime,
                        requestTime,
                        testCaseMeta.status,
                        response.status,
                        expectedHeaders,
                        actualHeaders,
                        differentHeaderItems
                )
            }
        }
        return new TestCaseResult(
                testCaseFullName,
                TestStatus.SUCCESS,
                System.currentTimeMillis() - beginTime,
                requestTime,
                testCaseMeta.status,
                response.status,
                expectedHeaders,
                response.headers
        )
    }

    def DifferentItem _compareBody(String key, expectedBody, actualBody) {
        // ignore
        if (expectedBody.is(Ignore.instance)) {
            return null
        }
        // empty
        if (expectedBody.is(NotEmpty.instance)) {
            if (actualBody) {
                return null
            } else {
                return new DifferentItem(key, expectedBody, actualBody)
            }
        }
        // map
        if (expectedBody instanceof Map) {
            if (!actualBody instanceof JSONObject) {
                return new DifferentItem(key, expectedBody, actualBody)
            }

            Map expectedMap = (Map) expectedBody
            JSONObject actualMap = (JSONObject) actualBody
            for (Map.Entry entry in expectedMap.entrySet()) {
                String expectedKey = entry.key
                def expectedElement = entry.value

                // ignore
                if (expectedElement.is(Ignore.instance)) {
                    continue
                }

                def actualElement = actualMap.get(expectedKey)
                if (!actualElement) {
                    return new DifferentItem("${key}.${expectedKey}", expectedElement, actualElement)
                }

                // not empty
                if (expectedElement.is(NotEmpty.instance)) {
                    continue
                }

                // 递归比较复合类型
                if (expectedElement instanceof List || expectedElement instanceof Map) {
                    def result = this._compareBody("${key}.${expectedKey}", expectedElement, actualElement)
                    if (result) {
                        return result
                    }
                }
                // 作为基本类型去比较
                else {
                    def result = this._compareValue("${key}.${expectedKey}", expectedElement, actualElement.toString())
                    if (result) {
                        return result
                    }
                }

            }
            return null
        }
        // list
        if (expectedBody instanceof List) {
            if (!actualBody instanceof JSONArray) {
                return new DifferentItem(key, expectedBody, actualBody)
            }
            List expectedLst = (List) expectedBody
            JSONArray actualLst = (JSONArray) actualBody

            for (int i = 0; i < expectedLst.size(); i++) {
                def expectedElement = expectedLst.get(i)

                // ignore
                if (expectedElement.is(Ignore.instance)) {
                    return null
                }

                // Size
                if (expectedElement instanceof Size) {
                    Size expectedSize = (Size) expectedElement
                    if (expectedSize.expectedSize == actualLst.size()) {
                        return null
                    } else {
                        return new DifferentItem(key, expectedLst, actualLst)
                    }
                }

                // 越界思密达!
                if (i > actualLst.size() - 1) {
                    return new DifferentItem(key, expectedLst, actualLst)
                }

                def actualElement = actualLst.get(i)

                // 遇到了NotEmpty
                if (expectedElement.is(NotEmpty.instance)) {
                    if (actualElement) {
                        continue
                    } else {
                        return new DifferentItem("${key}[${i}]", expectedLst, actualLst)
                    }
                }

                // 递归比较集合类型
                if (expectedElement instanceof List || expectedElement instanceof Map) {
                    def result = this._compareBody("${key}[${i}]", expectedElement, actualElement)
                    if (result) {
                        return result
                    }

                } else {
                    def result = this._compareValue("${key}[${i}]", expectedElement, actualElement.toString())
                    if (result) {
                        return result
                    }
                }
            }
            return null
        }

        return _compareValue(key, expectedBody, actualBody ? actualBody.toString() : "")
    }

    def DifferentItem _compareValue(String key, Object expectedVal, String actualVal, boolean ignoreCase = false) {
        // 期待值为忽略状态, 那么不必检查
        if (expectedVal.is(Ignore.instance)) {
            return null
        }

        actualVal = actualVal ? actualVal.trim() : ""

        // 期待值只要不为空即可
        if (expectedVal.is(NotEmpty.instance)) {
            if (!actualVal) {
                return new DifferentItem(key, expectedVal, actualVal)
            }
            return null
        }

        // 下面的情况都需要actualValue为非空
        if (!actualVal) {
            return new DifferentItem(key, expectedVal, actualVal)
        }

        // 期望值在某个范围区间
        if (expectedVal instanceof Range) {
            if (!(Integer.valueOf(actualVal) in expectedVal)) {
                return new DifferentItem(key, expectedVal, actualVal)
            }
            return null
        }

        // 期望值是正则
        if (expectedVal instanceof Regexp) {
            Regexp expectedValRegex = (Regexp) expectedVal
            if (!expectedValRegex.match(actualVal)) {
                return new DifferentItem(key, expectedVal, actualVal)
            }
            return null
        }

        // Predict
        if (expectedVal instanceof Predict) {
            Predict expectedValPredict = (Predict) expectedVal
            if (!Lang.cloneAndRun(expectedValPredict.predictLogic, this.testCaseContext, true, actualVal)) {
                return new DifferentItem(key, expectedVal, actualVal)
            }
            return null
        }

        // 未知类型一律转换为字符串比较
        expectedVal = expectedVal.toString()

        if (ignoreCase) {
            expectedVal = expectedVal.toLowerCase()
            actualVal = actualVal.toLowerCase()
        }
        if (!expectedVal.equals(actualVal)) {
            return new DifferentItem(key, expectedVal, actualVal)
        }
        return null
    }
}
