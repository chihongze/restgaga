package chihz.restgaga.runner

import chihz.restgaga.meta.GlobalNamespace
import chihz.restgaga.meta.MetaMapFactory
import org.apache.commons.collections.map.CaseInsensitiveMap

/**
 * <p>
 *     用于向将要运行的测试用例闭包传递上下文数据,
 *     包括以下几项:
 * </p>
 * <ol>
 *      <li>当前运行环境</li>
 *      <li>全局变量</li>
 *      <li>Http headers</li>
 *      <li>Http parameters</li>
 *      <li>Path variables</li>
 * </ol>
 */
public class TestCaseContext {

    /** 全局变量 */
    def final GlobalNamespace global = GlobalNamespace.instance

    /** 当前测试环境 */
    def final String currentEnv

    /** 最终运行测试所使用HTTP Headers */
    def final Map headers = new CaseInsensitiveMap()

    /** 最终运行测试所使用的HTTP请求参数 */
    def final Map params = new HashMap()

    /** 最终运行测试所使用的 Path Variable */
    def final Map pathVariables = new HashMap()

    /** Http 请求Body */
    def Object body = new HashMap()

    /** 用于记录测试上下文变量 */
    def final Map data = new HashMap()

    def TestCaseContext(String env) {
        this.currentEnv = env
    }

    def updateHeaders(MetaMapFactory... factories) {
        this._updateMap(this.headers, factories)
    }

    def updateParams(MetaMapFactory... factories) {
        this._updateMap(this.params, factories)
    }

    def updatePathVariables(MetaMapFactory... factories) {
        this._updateMap(this.pathVariables, factories)
    }

    def _updateMap(final Map targetMap, MetaMapFactory... factories) {
        factories.each({
            targetMap.putAll(it.getMap(this))
        })
    }

    def getAttribute(String attributeName) {
        this.data.get(attributeName)
    }

    def setAttribute(String attributeName, Object value) {
        this.data.put(attributeName, value)
    }
}
