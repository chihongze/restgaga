package chihz.restgaga.meta.loader

import chihz.restgaga.meta.*
import chihz.restgaga.meta.builder.dsl.*
import chihz.restgaga.meta.mgr.*
import chihz.restgaga.report.ReportAction
import chihz.restgaga.runner.RuntimeProperties
import com.mashape.unirest.http.Unirest
import org.apache.commons.lang.StringUtils

@Singleton
public class DSLMetaLoader extends BaseMetaLoader {

    def void load(String metaConfig, Map variables = null) {

        def script = """
        $metaConfig

        def methodMissing(String name, args) {
            NamedAction.newInstance(name, args[0])
        }
        """

        RuntimeProperties properties = RuntimeProperties.instance

        Binding shBinding

        if (variables) {
            shBinding = new Binding(variables)
        } else {
            shBinding = new Binding()
        }

        shBinding.setProperty("NamedAction", NamedAction.class)
        shBinding.setProperty("global", GlobalNamespace.instance)
        shBinding.setProperty("api", this.buildElement(APIMetaBuilder, APIMetaManager.instance))
        shBinding.setProperty("env", this.buildElement(EnvMetaBuilder, EnvMetaManager.instance))
        shBinding.setProperty("fixture", this.buildElement(FixtureMetaBuilder, FixtureMetaManager.instance))
        shBinding.setProperty("testcase", this.buildElement(TestCaseMetaBuilder, TestCaseMetaManager.instance))
        shBinding.setProperty("testsuite", this.buildElement(TestSuiteMetaBuilder, TestSuiteMetaManager.instance))
        shBinding.setProperty("globalHeaders", properties.globalHeaders)
        shBinding.setProperty("globalFixtures", properties.globalFixtures)
        shBinding.setProperty("Ignore", Ignore.instance)
        shBinding.setProperty("NotEmpty", NotEmpty.instance)
        shBinding.setProperty("Unirest", Unirest)
        shBinding.setProperty("regexp", { String pattern, boolean completeMatch = true ->
            new Regexp(pattern, completeMatch)
        })
        shBinding.setProperty("predict", { Closure predict -> new Predict(predict) })
        shBinding.setProperty("FORM_URL_TYPE", "application/x-www-form-urlencoded;charset=utf-8")
        shBinding.setProperty("JSON_TYPE", "application/json;charset=utf-8")
        shBinding.setProperty("Size", { int size -> new Size(size) })
        shBinding.setProperty("reportAction", { NamedAction action ->
            ReportActionMetaManager.instance.add(
                    new ReportActionMeta(action.name, action.action as ReportAction))
        })
        shBinding.setProperty("report", this.buildElement(ReportMetaBuilder, ReportMetaManager.instance))
        shBinding.setProperty("mock", { NamedAction action ->
            MockMetaManager.instance.add(
                    new MockMeta(action.name, (Closure) action.action))
        })
        shBinding.setProperty("mockInterceptor", { NamedAction action ->
            MockInterceptorMetaManager.instance.add(
                    new MockInterceptorMeta(action.name, (Closure) action.action))
        })

        def sh = new GroovyShell(shBinding)
        sh.evaluate(script)

        if (shBinding.hasVariable("connectionTimeout")) {
            RuntimeProperties.instance.connectionTimeout = shBinding.getVariable("connectionTimeout")
        }
        if (shBinding.hasVariable("socketTimeout")) {
            RuntimeProperties.instance.socketTimeout = shBinding.getVariable("socketTimeout")
        }
        if (shBinding.hasVariable("httpProxy")) {
            String httpProxy = shBinding.getVariable("httpProxy")
            def(host, port) = StringUtils.split(httpProxy, ':' as char)
            RuntimeProperties.instance.proxyHost = host
            RuntimeProperties.instance.proxyPort = Integer.valueOf(port)
        }
    }

    def buildElement(final Class builderType, final BaseMetaManager collector) {
        return { NamedAction action ->
            def BaseDSLBuilder builder = builderType.newInstance()
            collector.add(builder.build(action))
        }
    }

    def void checkAll() {
        EnvMetaManager.instance.check()
        FixtureMetaManager.instance.check()
        APIMetaManager.instance.check()
        TestCaseMetaManager.instance.check()
        TestSuiteMetaManager.instance.check()
    }

    def void cleanAll() {
        // 清理全局命名空间
        GlobalNamespace.instance.cleanAll()
        // 清理APIMeta
        APIMetaManager.instance.cleanAll()
        // 清理EnvMeta
        EnvMetaManager.instance.cleanAll()
        // 清理FixtureMeta
        FixtureMetaManager.instance.cleanAll()
        // 清理TestCaseMeta
        TestCaseMetaManager.instance.cleanAll()
        // 清理TestSuiteMeta
        TestSuiteMetaManager.instance.cleanAll()
        // 清理ReportMeta
        ReportMetaManager.instance.cleanAll()
        // 清理ReportActionMeta
        ReportActionMetaManager.instance.cleanAll()
        // 清理MockMeta
        MockMetaManager.instance.cleanAll()
        // 清理MockInterceptorMeta
        MockInterceptorMetaManager.instance.cleanAll()
        // 清理RuntimeProperties
        RuntimeProperties.instance.cleanAll()
    }
}
