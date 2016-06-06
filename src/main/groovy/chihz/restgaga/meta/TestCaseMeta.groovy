package chihz.restgaga.meta

/**
 * <p>用于描述某个API所属的测试用例</p>
 */
class TestCaseMeta extends BaseMeta {

    /** 测试用例所属的API */
    def final String api

    /** 该测试用例可以运行的环境 */
    def final String[] withEnv

    /** 请求Header */
    def final MetaMapFactory headers

    /** 请求参数 */
    def final MetaMapFactory params

    /** 请求Body */
    def final RequestBody body

    /** 路径参数 */
    def final MetaMapFactory pathVariables

    /** 期望的HTTP状态码 */
    def final int status;

    /** 期望的接口响应头 */
    def final MetaMapFactory responseHeaders

    /** 期望的测试接口响应结果 */
    def final MetaMapFactory responseBody

    /** 在测试之前执行的准备操作 */
    def final Closure setUp

    /** 在测试之后执行的清理操作 */
    def final Closure tearDown

    /** 运行该测试用例所需要使用的fixtures */
    def final MetaListFactory fixtures

    def TestCaseMeta(String name, String api, String[] withEnv,
                     MetaMapFactory headers, MetaMapFactory params, RequestBody body,
                     MetaMapFactory pathVariables, int status, MetaMapFactory responseHeaders,
                     MetaMapFactory responseBody, Closure setUp, Closure tearDown,
                     MetaListFactory fixtures) {
        super(name)
        this.api = api
        this.withEnv = withEnv
        this.headers = headers
        this.params = params
        this.body = body
        this.pathVariables = pathVariables
        this.status = status
        this.responseHeaders = responseHeaders
        this.responseBody = responseBody
        this.setUp = setUp
        this.tearDown = tearDown
        this.fixtures = fixtures
    }
}
