package chihz.restgaga.meta.mgr

import chihz.restgaga.meta.TestSuiteMeta

/**
 * 用于管理所有的TestSuiteMeta对象
 */
@Singleton
class TestSuiteMetaManager extends BaseMetaManager {

    def check() throws InvalidMetaObjectException {
        def testcaseMgr = TestCaseMetaManager.instance
        def allTestsMgr = new MetaManagerChain([this, testcaseMgr])
        for (TestSuiteMeta meta in this.metaMap.values()) {
            for (String test in meta.tests) {
                if (!allTestsMgr.existed(test)) {
                    throw new InvalidMetaObjectException(
                            "Can not found test '$test' in test suite ${meta.name}")
                }
            }
        }
    }
}
