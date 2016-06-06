package chihz.restgaga.runner

import chihz.restgaga.meta.*
import chihz.restgaga.meta.loader.DSLMetaLoader
import chihz.restgaga.meta.mgr.*
import chihz.restgaga.report.TextReportAction
import com.mashape.unirest.http.Unirest
import groovy.io.FileType
import org.apache.commons.lang.StringUtils
import org.apache.http.HttpHost

public class TestRunner extends Runner {

    // 项目目录
    def final String projectDir

    // 测试目标
    def final String targetTest

    def final DSLMetaLoader dslMetaLoader = DSLMetaLoader.instance

    def final MetaManagerChain testMetaManagerChain = new MetaManagerChain([
            TestCaseMetaManager.instance,
            TestSuiteMetaManager.instance,
            APIMetaManager.instance
    ])

    def TestRunner(String projectDir, String targetTest) {
        this.projectDir = projectDir
        this.targetTest = targetTest
    }

    def run() {
        _loadMeta()
        _checkMeta()
        _loadUnirestConfig()
        TestSuiteResult result = _run()
        _report(result)
    }

    def _loadMeta() {
        // load all test groovy scripts under the project dir
        new File(this.projectDir).eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith(".groovy")) {
                String[] parts = StringUtils.split(it.name, '.')
                if (parts.length >= 3) {
                    // 只加载特定环境下的DSL
                    String env = parts[-2]
                    if (RuntimeProperties.instance.currentEnv == env) {
                        dslMetaLoader.load(it.text)
                    }
                } else {
                    dslMetaLoader.load(it.text)
                }
            }
        }
        _addDefaultReport()
    }

    def _addDefaultReport() {
        if (!ReportActionMetaManager.instance.existed("text")) {
            ReportActionMetaManager.instance.add(new ReportActionMeta("text", new TextReportAction()))
        }
        if (!ReportMetaManager.instance.existed("default")) {
            ReportMetaManager.instance.add(new ReportMeta("default", "text", new String[0],
                    [output: System.out]))
        }
    }

    def _loadUnirestConfig() {
        Unirest.setTimeouts(
                RuntimeProperties.instance.connectionTimeout,
                RuntimeProperties.instance.socketTimeout
        )
        if (RuntimeProperties.instance.proxyHost) {
            Unirest.setProxy(new HttpHost(
                    RuntimeProperties.instance.proxyHost,
                    RuntimeProperties.instance.proxyPort
            ))
        }
    }

    def _checkMeta() {
        this.dslMetaLoader.checkAll()
    }

    def _run() {
        BaseMeta metaObj = this.testMetaManagerChain.getMetaObject(targetTest)
        if (metaObj == null) {
            if (targetTest == "all") {
                def allApi = APIMetaManager.instance.metaMap.keySet().collect { it }
                if (!allApi) {
                    throw new IllegalArgumentException("There are no testcase to run.")
                }
                metaObj = new TestSuiteMeta("all", allApi, false)
            } else {
                throw new IllegalArgumentException("Unknown target test '${targetTest}' .".toString())
            }
        }

        // test case
        if (metaObj instanceof TestCaseMeta) {
            TestCaseRunner testCaseRunner = new TestCaseRunner(metaObj)
            TestCaseResult result = testCaseRunner.run()
            return new TestSuiteResult(metaObj.name, [result] as RunnerResult[], result.usedTime)
        }
        // test suite
        else if (metaObj instanceof TestSuiteMeta || metaObj instanceof APIMeta) {
            TestSuiteRunner testSuiteRunner = new TestSuiteRunner(metaObj)
            return testSuiteRunner.run()
        }
        // unknown
        else {
            throw new RuntimeException("Unknown target type: ${targetTest}")
        }
    }

    def _report(TestSuiteResult result) {
        def allReportMeta = ReportMetaManager.instance.metaMap.values()
        allReportMeta.each({ ReportMeta it ->
            ReportActionMeta reportAction = ReportActionMetaManager.instance.getMetaObject(it.action)
            if (!it.env || RuntimeProperties.instance.currentEnv in it.env) {
                reportAction.action.report(result, it.args)
            }
        })
    }
}
