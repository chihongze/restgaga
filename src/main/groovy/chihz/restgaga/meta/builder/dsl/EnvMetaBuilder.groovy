package chihz.restgaga.meta.builder.dsl

import chihz.restgaga.meta.EnvMeta
import chihz.restgaga.meta.MetaListFactory
import chihz.restgaga.meta.MetaMapFactory


/**
 * 该构建者允许使用以下的DSL来构建EnvMeta对象, 例如:
 *
 * <pre>
 *     builder = new EnvMetaBuilder()
 *     builder.build test {*         baseUrl "http://test.hehe.com"
 *         headers (
 *             Accept: "application/json"
 *             "Accept-Language": "en-US"
 *         )
 *         fixtures << [ "sign", "token" ]
 *}* </pre>
 */
class EnvMetaBuilder extends BaseDSLBuilder {

    def String baseUrl = null

    def MetaMapFactory _headers = new MetaMapFactory()

    def MetaListFactory fixtures = new MetaListFactory()

    def baseUrl(String baseUrl) {
        this.baseUrl = baseUrl
        this
    }

    def headers(action = Collections.emptyMap()) {
        this._headers.call(action)
        this
    }

    def newInstance() {
        if (!baseUrl) {
            throw new IllegalStateException("env($name): baseUrl not allow empty")
        }
        return new EnvMeta(
                this.name,
                this.baseUrl,
                this._headers,
                this.fixtures
        )
    }
}
