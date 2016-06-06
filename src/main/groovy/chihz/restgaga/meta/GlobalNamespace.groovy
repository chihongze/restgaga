package chihz.restgaga.meta

import java.util.concurrent.ConcurrentHashMap

/**
 * 全局命名空间, 在这里保存整个测试都需要用到的变量, 以及完成测试的初始化工作
 * <pre>
 *     def global =
 * </pre>
 */
@Singleton
class GlobalNamespace {

    def final @Delegate
    Map data = new ConcurrentHashMap()

    def Closure initLogic = null

    def call(initLogic) {
        if (!(initLogic instanceof Closure)) {
            throw new IllegalArgumentException("Only support Closure argument.")
        }
        this.initLogic = initLogic
    }

    def init(String env = null) {
        if (initLogic == null) {
            return
        }
        initLogic.delegate = this
        initLogic(env)
    }

    def void cleanAll() {
        data.clear()
        initLogic = null
    }
}
