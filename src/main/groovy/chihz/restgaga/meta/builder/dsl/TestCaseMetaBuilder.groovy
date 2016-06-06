package chihz.restgaga.meta.builder.dsl

import chihz.restgaga.meta.*
import chihz.restgaga.runner.RuntimeProperties

/**
 * 使用该Builder可以通过以下形式的DSL构建TestCaseMeta对象
 *
 * <pre>
 *     def builder = new TestCaseMetaBuilder()
 *     builder.build login {*         success
 *         withEnv "test", "pro"
 *         headers (
 *             Accept: "application/json"
 *         )
 *         params (
 *             username: "samchi",
 *             password: "123456"
 *         )
 *         pathVariables (
 *
 *         )
 *         responseHeaders (
 *             token: "xxxxxx"
 *         )
 *         fixtures ^ [ "sign", "token" ]
 *}* </pre>
 */
class TestCaseMetaBuilder extends BaseDSLBuilder {

    def String _api = null

    def String[] _withEnv

    def MetaMapFactory _headers = new MetaMapFactory()

    def MetaMapFactory _params = new MetaMapFactory()

    def RequestBody _body = null

    def MetaMapFactory _pathVariables = new MetaMapFactory()

    def int _status = 200

    def MetaMapFactory _responseHeaders = new MetaMapFactory()

    def MetaMapFactory _responseBody = new MetaMapFactory()

    def Closure _setUp = null

    def Closure _tearDown = null

    def MetaListFactory fixtures = new MetaListFactory()


    def api(String api) {
        this._api = api
    }

    def withEnv(String... envList) {
        this._withEnv = envList
        this
    }

    def headers(action = Collections.emptyMap()) {
        this._headers.call(action)
        this
    }

    def params(action = Collections.emptyMap()) {
        this._params.call(action)
        this
    }

    def mapBody(action = Collections.emptyMap()) {
        MetaMapFactory body = new MetaMapFactory()
        body.call(action)
        this._body = new MapRequestBody(body)
        this
    }

    def bytesBody(action = null) {
        if (action == null) {
            action = new byte[0]
        }
        this._body = new BytesRequestBody(action)
        this
    }

    def stringBody(String str = null) {
        if (str == null) {
            return
        }
        this.bytesBody(str)
    }

    def fileBody(String filePath = null) {
        if (filePath == null) {
            return
        }
        def file = new File(filePath)
        if (file.isAbsolute()) {
            this.bytesBody(new File(filePath))
        } else {
            file = new File(RuntimeProperties.instance.currentProjectDir, filePath)
            this.bytesBody(file)
        }

    }

    def pathVariables(action = Collections.emptyMap()) {
        this._pathVariables.call(action)
        this
    }

    def responseHeaders(action = Collections.emptyMap()) {
        this._responseHeaders.call(action)
        this
    }

    def responseBody(action = Collections.emptyMap()) {
        this._responseBody.call(action)
        this
    }

    def status(int status) {
        this._status = status
        this
    }

    def setUp(Closure action) {
        this._setUp = action
        this
    }

    def tearDown(Closure action) {
        this._tearDown = action
        this
    }

    def build(action = null) {
        if (action == null) {
            return newInstance()
        }
        if (action instanceof NamedAction) {
            this._api = action.name
            action = action.action
        }
        action.delegate = this
        action()
        newInstance()
    }

    def propertyMissing(String propertyName) {
        this.name = propertyName
    }

    def newInstance() {
        new TestCaseMeta(name, _api, _withEnv, _headers, _params, _body,
                _pathVariables, _status, _responseHeaders, _responseBody, _setUp, _tearDown, fixtures)
    }
}
