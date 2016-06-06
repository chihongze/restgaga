package chihz.restgaga.meta.builder.dsl

import chihz.restgaga.meta.APIMeta
import chihz.restgaga.meta.MetaListFactory
import chihz.restgaga.meta.MetaMapFactory
import chihz.restgaga.meta.RequestMethod

/**
 * <p>使用该类允许通过以下DSL来构建APIMeta对象</p>
 * <pre>
 *     def builder = new APIMetaBuilder()
 *     builder.build login {*          post '/user/login'
 *          headers (
 *              Accept: "application/json"
 *          )
 *          params (
 *              username: "samchi",
 *              password: "123456"
 *          )
 *          pathVariables (
 *
 *          )
 *          fixtures << [ 'sign', 'token' ]
 *}* </pre>
 */
class APIMetaBuilder extends BaseDSLBuilder {

    def RequestMethod _method = null

    def String _path = null

    def MetaMapFactory _headers = new MetaMapFactory()

    def MetaMapFactory _params = new MetaMapFactory()

    def MetaMapFactory _pathVariables = new MetaMapFactory()

    def MetaListFactory fixtures = new MetaListFactory()

    def method(RequestMethod method) {
        this._method = method
        this
    }

    def path(String path) {
        this._path = path
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

    def pathVariables(action = Collections.emptyMap()) {
        this._pathVariables.call(action)
        this
    }

    def get(String path) {
        this._method = RequestMethod.GET
        this._path = path
        this
    }

    def post(String path) {
        this._method = RequestMethod.POST
        this._path = path
        this
    }

    def put(String path) {
        this._method = RequestMethod.PUT
        this._path = path
        this
    }

    def delete(String path) {
        this._method = RequestMethod.DELETE
        this._path = path
        this
    }

    def newInstance() {
        new APIMeta(name, _method, _path, _headers,
                _params, _pathVariables, fixtures)
    }
}
