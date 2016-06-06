package chihz.restgaga.runner

import chihz.restgaga.meta.APIMeta
import chihz.restgaga.meta.BaseMeta
import chihz.restgaga.meta.TestCaseMeta
import chihz.restgaga.meta.TestSuiteMeta
import chihz.restgaga.meta.mgr.APIMetaManager
import chihz.restgaga.meta.mgr.MetaManagerChain
import chihz.restgaga.meta.mgr.TestCaseMetaManager
import chihz.restgaga.meta.mgr.TestSuiteMetaManager

import java.util.concurrent.Callable
import java.util.concurrent.Future

public class TestSuiteRunner extends Runner {

    def final TestSuiteMeta testSuiteMeta

    def final MetaManagerChain metaMgrChain = new MetaManagerChain([
            TestCaseMetaManager.instance,
            TestSuiteMetaManager.instance,
            APIMetaManager.instance
    ])

    def final TestCaseMetaManager testCaseMetaManager = TestCaseMetaManager.instance

    def final RuntimeProperties runtimeProperties = RuntimeProperties.instance

    def TestSuiteRunner(BaseMeta metaObj) {
        if (metaObj instanceof TestSuiteMeta) {
            this.testSuiteMeta = metaObj
        } else if (metaObj instanceof APIMeta) {
            def allTestCase = testCaseMetaManager.getTestCaseByApi(metaObj.name).collect({
                "${metaObj.name}.${it.name}"
            })
            this.testSuiteMeta = new TestSuiteMeta(metaObj.name, allTestCase, true)
        } else {
            throw new IllegalArgumentException("Only support TestSuiteMeta and APIMeta argument.")
        }
    }

    def TestSuiteResult run() {
        long beginTime = System.currentTimeMillis()
        List tests = testSuiteMeta.tests
        if (!tests) {
            return new TestSuiteResult(testSuiteMeta.name, new RunnerResult[0], 0)
        }

        BaseMeta[] metaObjects = tests.collect({
            BaseMeta meta = metaMgrChain.getMetaObject(it)
            if (meta == null) {
                throw new RuntimeException(
                        "Unknown meta object '${it}' in test suite ${testSuiteMeta.name} .")
            }
            return meta
        })

        RunnerResult[] results

        if (testSuiteMeta.allConcurrent) {
            results = this._concurrentRun(metaObjects)
        } else {
            results = this._commonRun(metaObjects)
        }

        new TestSuiteResult(testSuiteMeta.name, results, System.currentTimeMillis() - beginTime)
    }

    def RunnerResult[] _commonRun(BaseMeta[] metaObjects) {
        metaObjects.collect({
            this._runMeta(it)
        })
    }

    def RunnerResult[] _concurrentRun(BaseMeta[] metaObjects) {
        Future[] futures = metaObjects.collect({
            def meta = it
            runtimeProperties.executor.submit({
                this._runMeta(meta)
            } as Callable<RunnerResult>)
        })
        futures.collect({ it.get() }) as RunnerResult[]
    }

    def RunnerResult _runMeta(BaseMeta meta) {
        if (meta instanceof TestCaseMeta) {
            return new TestCaseRunner(meta).run()
        } else if (meta instanceof TestSuiteMeta || meta instanceof APIMeta) {
            return new TestSuiteRunner(meta).run()
        } else {
            throw new RuntimeException(
                    "Unknown meta type '${meta.class}' in test suite ${testSuiteMeta.name} .")
        }
    }

}
