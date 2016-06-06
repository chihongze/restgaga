package chihz.restgaga.meta.mgr

import chihz.restgaga.meta.TestCaseMeta
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap

/**
 * 用于管理所有的TestCaseMeta对象
 */
@Singleton
public class TestCaseMetaManager extends BaseMetaManager {

    def final Multimap apiBasedMap = ArrayListMultimap.create()

    def add(TestCaseMeta testcaseMeta) throws MetaObjectExistedException {
        def name = testcaseMeta.api + "." + testcaseMeta.name
        if (this.metaMap.containsKey(name)) {
            throw new MetaObjectExistedException("Meta object $name already existed.")
        }
        this.metaMap.put(name, testcaseMeta)
        this.apiBasedMap.put(testcaseMeta.api, testcaseMeta)
    }

    def List getTestCaseByApi(String apiName) {
        return this.apiBasedMap.get(apiName)
    }

    def check() throws InvalidMetaObjectException {
        def apiMgr = APIMetaManager.instance
        def envMgr = EnvMetaManager.instance

        for (TestCaseMeta m in this.metaMap.values()) {
            // check api
            if (!apiMgr.existed(m.api)) {
                throw new InvalidMetaObjectException(
                        "Unknown api '${m.api}' in testcase '${m.api}.${m.name}' ")
            }
            // check env
            if (m.withEnv) {
                for (String env in m.withEnv) {
                    if (!envMgr.existed(env)) {
                        throw new InvalidMetaObjectException(
                                "Unknown env '${env}' in testcase '${m.api}.${m.name}' ")
                    }
                }
            }
        }
    }

    def void cleanAll() {
        super.cleanAll()
        this.apiBasedMap.clear()
    }
}
