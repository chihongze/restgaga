package chihz.restgaga.runner

import chihz.restgaga.meta.MetaListFactory
import chihz.restgaga.meta.MetaMapFactory

import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * 运行时属性
 */
@Singleton
class RuntimeProperties {

    def String currentEnv = "test"

    def MetaMapFactory globalHeaders = new MetaMapFactory()

    def MetaListFactory globalFixtures = new MetaListFactory()

    def Executor executor = Executors.newFixedThreadPool(Runtime.runtime.availableProcessors() * 2)

    def String currentProjectDir = new File(".").getAbsolutePath()

    def long connectionTimeout = 0

    def long socketTimeout = 0

    def String proxyHost = null

    def int proxyPort = 0

    def void cleanAll() {
        this.globalHeaders = new MetaMapFactory()
        this.globalFixtures = new MetaListFactory()
    }
}
